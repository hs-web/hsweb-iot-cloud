package org.hswebframework.iot.mqtt.emulator;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static java.lang.System.out;

/**
 * @author zhouhao
 * @since 1.0
 */
public class MQTTEmulatorApplication {
    private static AtomicLong connectCounter      = new AtomicLong();
    private static AtomicLong connectErrorCounter = new AtomicLong();

    private static AtomicLong commandCounter = new AtomicLong();

    private static AtomicLong reportCounter      = new AtomicLong();
    private static AtomicLong reportErrorCounter = new AtomicLong();

    private static AtomicLong replyCounter      = new AtomicLong();
    private static AtomicLong replyErrorCounter = new AtomicLong();


    private static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final static Queue<Runnable> runnerQueue = new LinkedBlockingDeque<>();

    private static List<MqttClient> clients;

    private static Map<String, String> argsMap;

    private static List<JSONObject> reportData;

    private static Map<String, List<JSONObject>> replyData;

    @SuppressWarnings("all")
    public static void main(String[] args) throws Exception {
        argsMap = argsToMap(args);
        if ("true".equals(argsMap.get("help"))) {
            printHelp();
            return;
        }
        String[] servers = argsMap
                .computeIfAbsent("servers", key -> "tcp://127.0.0.1:1883")
                .split("[,]");

        String clientsFile = argsMap.computeIfAbsent("clients", key -> "./data/clients.txt");

        if (new File("./data/report.json").exists()) {
            String report = Files.readAllLines(Paths.get("./data/report.json"))
                    .stream()
                    .reduce((s1, s2) -> String.join("\n", s1, s2))
                    .orElse("[]");

            reportData = (List) JSON.parseArray(report);
        }

        if (new File("./data/reply.json").exists()) {
            String reply = Files.readAllLines(Paths.get("./data/reply.json"))
                    .stream()
                    .reduce((s1, s2) -> String.join("\n", s1, s2))
                    .orElse("{}");
            replyData = (Map) JSON.parseObject(reply);
        }

        int skip = Integer.parseInt(argsMap.computeIfAbsent("skip", key -> "0"));

        int limit = Integer.parseInt(argsMap.computeIfAbsent("limit", key -> String.valueOf(Integer.MAX_VALUE)));
        boolean disableReport = Boolean.parseBoolean(argsMap.computeIfAbsent("disableReport", key -> "false"));
        Boolean.parseBoolean(argsMap.computeIfAbsent("autoReconnect", key -> "true"));

        out.println("使用配置:\n" + JSON.toJSONString(argsMap, SerializerFeature.PrettyFormat));

        Runtime.getRuntime().addShutdownHook(new Thread(MQTTEmulatorApplication::printResult));
        out.println("建立连接中....");
        List<String> allLine = Files.lines(Paths.get(clientsFile))
                .skip(skip)
                .limit(limit)
                .collect(Collectors.toList());

        clients = allLine.parallelStream()
                .map(line -> {
                    String[] np = line.split("[:]");
                    String errorMessage = "";
                    try {
                        return connect(servers, np[0], np[1]);
                    } catch (Exception e) {
                        errorMessage = getCauseMessage(e);
                        connectErrorCounter.incrementAndGet();
                    } finally {
                        out.print("\r已建立链接:" + connectCounter + "/" + allLine.size() + ",失败:" + connectErrorCounter + (errorMessage.isEmpty() ? "" : "(" + errorMessage + ")"));
                        errorMessage = "";
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        out.println("\n建立连接完成,客户端数量:" + clients.size());
        if (clients.isEmpty()) {
            System.exit(0);
        }
        if (!disableReport) {
            executorService.execute(() -> {
                for (; ; ) {
                    Random random = new Random();
                    int replyClient = Math.min(Integer.parseInt(argsMap.getOrDefault("reportClientsSize", String.valueOf(random.nextInt(clients.size())))),
                            clients.size());
                    int replyInterval = Integer.parseInt(argsMap.getOrDefault("reportInterval", "10000"));
                    try {
                        Thread.sleep(replyInterval);
                    } catch (@SuppressWarnings("all") InterruptedException e) {
                    }
                    if (replyClient == 0) {
                        continue;
                    }
                    out.println("执行数据上报,客户端数量:" + replyClient + ",当前客户端总数量:" + clients.size());

                    for (int i = 0; i < replyClient; i++) {
                        int clientIndex = random.nextInt(replyClient);
                        if (clients.size() > clientIndex) {
                            addRunner(() -> doReport(clients.get(clientIndex)));
                        }
                    }
                }
            });
        }
        startRunner();
    }

    private static void addRunner(Runnable runnable) {
        runnerQueue.add(runnable);
        synchronized (runnerQueue) {
            runnerQueue.notifyAll();
        }
    }

    private static void startRunner() {
        for (int i = 0; i < 4; i++) {
            executorService.execute(() -> {
                for (; ; ) {
                    try {
                        Runnable runnable = runnerQueue.poll();
                        if (null == runnable) {
                            synchronized (runnerQueue) {
                                runnerQueue.wait();
                                runnable = runnerQueue.poll();
                            }
                        }
                        if (null == runnable) {
                            Thread.sleep(100);
                            continue;
                        }
                        runnable.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private static void doReport(MqttClient client) {
        if (null != reportData && null != client && client.isConnected()) {
            JSONObject randomData = reportData.get(new Random().nextInt(reportData.size()));
            if (null != randomData) {
                JSONObject data = new JSONObject(randomData);
                data.put("messageId", System.nanoTime());
                try {
                    reportCounter.incrementAndGet();
                    client.publish("report", data.toJSONString().getBytes(), 0, false);
                } catch (MqttException e) {
                    reportErrorCounter.incrementAndGet();
                    out.println("上报数据失败:" + e.getMessage());
                }
            }
        }
    }

    private static void doReply(MqttClient client, String message) {
        if (null != replyData && null != client && client.isConnected()) {
            JSONObject object = JSON.parseObject(message);
            String operation = object.getString("operation");
            long messageId = object.getLong("messageId");
            List<JSONObject> allData = replyData.get(operation);
            if (null != allData) {
                Random random = new Random();
                JSONObject conf = allData.get(random.nextInt(allData.size()));
                JSONObject reply = new JSONObject(conf);
                List<Integer> delays = (List) conf.getJSONArray("delay");
                if (delays != null) {
                    int min = Math.min(delays.get(0), 0);
                    int max = delays.size() > 1 ? Math.max(delays.get(1), 1000) : 1000;
                    try {
                        Thread.sleep(Math.max(min, random.nextInt(max)));
                    } catch (InterruptedException e) {
                    }
                }
                reply.put("messageId", messageId);
                reply.remove("delay");
                try {
                    replyCounter.incrementAndGet();
                    out.println("回复请求:" + reply);
                    client.publish("reply", reply.toJSONString().getBytes(), 0, false);
                } catch (MqttException e) {
                    replyErrorCounter.incrementAndGet();
                    out.println("回复失败:" + e.getMessage());
                }
            }
        }
    }

    private static void printHelp() throws Exception {
        if (new File("./help.txt").exists()) {
            Files.lines(Paths.get("./help.txt")).forEach(out::println);
        } else {
            out.println("未找到帮助文件!");
        }
    }


    private static String getCauseMessage(Throwable e) {
        Throwable tmp = e.getCause() == null ? e : e.getCause();
        Throwable error = tmp;
        while (tmp != null) {
            tmp = tmp.getCause();
            if (tmp != null) {
                error = tmp;
            }
        }
        return error != null ? error.getMessage() : "";
    }

    public static MqttClient connect(String[] servers, String clientId, String password) throws Exception {
        boolean autoReconnect = Boolean.parseBoolean(argsMap.getOrDefault("autoReconnect", "true"));
        MqttClient client = new MqttClient(servers[0], clientId, new MqttDefaultFilePersistence(System.getProperty("java.io.tmpdir") + "/mqtt/clients"));
        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(clientId);
        options.setServerURIs(servers);
        options.setPassword(password.toCharArray());
        options.setConnectionTimeout(20);
        options.setCleanSession(true);
        options.setKeepAliveInterval(20);
        client.connect(options);
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                System.err.println("连接已断开[" + clientId + "]");
                connectErrorCounter.incrementAndGet();
                clients.remove(client);
                if (autoReconnect) {
                    try {
                        connect(servers, clientId, password);
                    } catch (Exception e) {
                        out.println("重连客户端[" + clientId + "]失败:" + getCauseMessage(e));
                    }
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });
        client.subscribe("execute", (topic, message) -> {
            out.println("[" + clientId + "]接收到mqtt指令:" + message + "");
            commandCounter.incrementAndGet();
            addRunner(() -> doReply(client, new String(message.getPayload())));
        });
        connectCounter.incrementAndGet();
        return client;
    }

    public static Map<String, String> argsToMap(String[] args) {
        return Arrays.stream(args)
                .map(str -> str.split("[=]"))
                .collect(Collectors.toMap(str -> str[0], str -> str.length > 1 ? str[1] : "true"));
    }

    public static void printResult() {
        out.println("上报数据数量:" + reportCounter + "次,失败:" + reportErrorCounter);

        out.println("接收到指令次数:" + commandCounter + ",回复数据数量:" + replyCounter + "次,失败:" + replyErrorCounter);

        out.println("连接次数:" + connectCounter + "次,失败:" + connectErrorCounter);

    }
}
