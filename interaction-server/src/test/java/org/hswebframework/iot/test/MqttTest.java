package org.hswebframework.iot.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;
import org.hswebframework.web.id.IDGenerator;

import java.util.ArrayList;
import java.util.List;

/**
 * @author zhouhao
 * @since 1.1.0
 */
public class MqttTest {

    @SneakyThrows
    public static void main(String[] args) {
        MqttClient client = new MqttClient("tcp://127.0.0.1:1883", "test",
                new MqttDefaultFilePersistence("./target/tmp"));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName("test");
        options.setPassword("test".toCharArray());
        options.setConnectionTimeout(5);
        options.setMaxInflight(999999);
        client.connect(options);

        client.subscribe("execute", (topic, message) -> {
            JSONObject msg = JSON.parseObject(message.toString());
            System.out.println(message.toString());
            new Thread(() -> {
                try {
                    //延迟一秒回复消息
                    Thread.sleep(1000);
                    JSONObject object = new JSONObject();
                    object.put("messageId", msg.get("messageId"));
                    object.put("operation", msg.get("operation"));
                    object.put("status", 0);
                    client.publish("reply", object.toString().getBytes(), 0, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

}
