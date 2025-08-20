package com.app.mqttproject;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String ip = "tcp://121.199.5.101:1883";//服务器地址
    private String userName = "xiao";//用户名
    private String passWord = "12345678";//密码
    private String Id = "app" + System.currentTimeMillis(); //clientld很重要,不能重复,否则就会连不上,所以我定义成app+当前时间
    private String mqtt_sub_topic = "/iotsoil/post";//需要订阅的主题
    private String mqtt_pub_topic = "xiao";//需要发布的主题
    private MqttClient mqtt_client;//创建一个mqtt_client对象
    MqttConnectOptions options;
    private TextView tv_message;
    private Button btn_light;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv_message = findViewById(R.id.tv_message);
        btn_light = findViewById(R.id.btn_light);
        //初始化mqtt并连接
        mqtt_init_Connect();
        //设置回调
        mqtt_client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                //连接丢失
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                //接收到消息
                Log.d(TAG, "messageArrived: " + new String(message.getPayload()));
                String msg = new String(message.getPayload());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv_message.setText(msg);
                    }
                });
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                //消息发送完成

            }
        });

        //设置按钮点击事件
        btn_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                publish(mqtt_pub_topic,"1");
            }
        });

    }

    //初始化mqtt连接
    public void mqtt_init_Connect() {
        try {
            //实例化mqtt_client,填入我们定义的serverUri和clientId,然后MemoryPersistence设置clientid的保存形式,默认为以内存保存
            mqtt_client = new MqttClient(ip, Id, new MemoryPersistence());
            //创建并实例化一个MQTT的连接参数对象
            options = new MqttConnectOptions();
            //然后设置对应的参数
            options.setUserName(userName);//设置连接的用户名
            options.setPassword(passWord.toCharArray());//设置连接的密码
            options.setConnectionTimeout(30);//设置连接超时时间,单位为秒
            options.setKeepAliveInterval(50);//设置心跳时间,单位为秒
            options.setAutomaticReconnect(true);//设置是否自动重连
            options.setCleanSession(false);//设置清除session,设置为true表示每次连接到服务器都以新的身份连接,设置为false表示服务器会保留客户端的连接记录
            /*初始化成功之后就开始连接*/
            Connect();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_LONG).show();
        }
    }
    //连接mqtt服务器
    public void Connect() {
        try {
            Toast.makeText(MainActivity.this, "开始建立连接", Toast.LENGTH_LONG).show();
            mqtt_client.connect(options);
            mqtt_client.subscribe(mqtt_sub_topic);//订阅主题
            Toast.makeText(this, "连接成功啦....!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
            Log.d("MQTTCon", "mqtt连接失败");
        }

    }
    //发布消息
    public void publish(String topic,String msg) {
        if (mqtt_client == null || !mqtt_client.isConnected()) {
            Log.d(TAG, "MQTT client is not connected");
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(msg.getBytes());//设置消息内容
        message.setQos(0);//设置消息质量
        try {
            mqtt_client.publish(topic, message);//发布消息
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}