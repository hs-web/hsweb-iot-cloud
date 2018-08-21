package org.hswebframework.iot.test;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import org.hswebframework.web.Maps;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardProtocolFamily;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhouhao
 * @since 1.1.0
 */
public class UdpTest {

    @SneakyThrows
    public static void main(String[] args) {
        DatagramChannel channel = DatagramChannel.open(StandardProtocolFamily.INET);
        SocketAddress server = new InetSocketAddress("127.0.0.1", 5010);
        channel.connect(server);
        JSONObject data = new JSONObject();
        data.put("type", "register");
        data.put("data", Maps.buildMap()
                .put("clientId", "test")
                .put("username", "test")
                .put("password", "test")
                .get());

        channel.send(ByteBuffer.wrap(JSON.toJSONBytes(data)), server);

        int bufferLength = 1024;
        ByteBuffer buffer = ByteBuffer.allocate(bufferLength);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        AtomicLong pingCounter = new AtomicLong();
        AtomicLong pongCounter = new AtomicLong();

        executorService.scheduleWithFixedDelay(() -> {
            try {
                pingCounter.addAndGet(1);
                channel.send(ByteBuffer.wrap("{\"type\":\"ping\"}".getBytes()), server);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 2, 5, TimeUnit.SECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("ping count:" + pingCounter.get() + " pong count:" + pongCounter.get());
                channel.send(ByteBuffer.wrap("{\"type\":\"unregister\"}".getBytes()), server);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }));
        while (true) {
            buffer.clear();
            int len = channel.read(buffer);
            String result = new String(buffer.array(), 0, len);
            if (result.contains("ping")) {
                pongCounter.addAndGet(1);
            }
            System.out.println(result);
        }

    }
}
