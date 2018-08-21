package org.hswebframework.iot.interaction.vertx.udp;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.datagram.DatagramSocket;
import io.vertx.core.datagram.DatagramSocketOptions;
import io.vertx.core.net.SocketAddress;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.iot.interaction.authority.DeviceAuthorityService;
import org.hswebframework.iot.interaction.events.CommandReplyEvent;
import org.hswebframework.iot.interaction.events.DeviceReportEvent;
import org.hswebframework.iot.interaction.vertx.client.Client;
import org.hswebframework.iot.interaction.vertx.client.ClientRepository;
import org.hswebframework.iot.interaction.vertx.client.message.ClientMessage;
import org.hswebframework.iot.interaction.vertx.client.message.MessageCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;


/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class VertxUDPServer extends AbstractVerticle {

    @Autowired
    public DeviceAuthorityService authorityService;

    private DatagramSocket socket;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    @Value("${vertx.udp.host:0.0.0.0}")
    private String host = "0.0.0.0";

    @Value("${vertx.udp.port:5010}")
    private int port = 5010;

    @Override
    public void start() throws Exception {
        DatagramSocketOptions options = new DatagramSocketOptions();
        socket = vertx.createDatagramSocket(options);

        socket.listen(port, host, result -> {
            if (result.succeeded()) {
                DatagramSocket datagramSocket = result.result();
                log.debug("UDP server started on port {}", 5010);
                datagramSocket.handler(packetResult -> {
                    Buffer data = packetResult.data();
                    SocketAddress sender = packetResult.sender();
                    //根据ip和port作为id
                    String id = sender.host() + ":" + sender.port();

                    log.info("接受到UDP客户端:[{}]发送的数据:{}", sender, data);
                    byte[] dataBytes = data.getBytes();
                    UDPMessage messageObject;
                    try {
                        messageObject = JSON.parseObject(dataBytes, UDPMessage.class);
                    } catch (Exception e) {
                        sendMessageCode(sender, MessageCode.PARAM_FORMAT_ERROR);
                        return;
                    }
                    //注册
                    if ("register".equals(messageObject.getType())) {
                        tryRegister(messageObject, sender);
                        return;
                    }
                    Client client = clientRepository.getClient(id);
                    if (client == null) {
                        sendMessageCode(sender, MessageCode.UN_REGISTERED_CLIENT);
                        return;
                    }
                    ClientMessage clientMessage = null;
                    switch (messageObject.getType()) {
                        case "ping":
                            client.ping();
                            sendMessageCode(sender, MessageCode.SUCCESS, "ping");
                            break;
                        case "reply":
                            clientMessage = JSON.parseObject(dataBytes, CommandReplyEvent.class);
                            break;
                        case "unregister":
                            clientRepository.unregister(id);
                            sendMessageCode(sender, MessageCode.SUCCESS, "unregister");
                            break;
                        case "report":
                            clientMessage = JSON.parseObject(dataBytes, DeviceReportEvent.class);
                            break;
                        default:
                            //不支持的消息类型
                            sendMessageCode(sender, MessageCode.UN_SUPPORT_TYPE);
                            break;
                    }

                    if (clientMessage != null) {
                        eventPublisher.publishEvent(clientMessage);
                    }

                }).endHandler(end -> log.debug("end handle udp"));
            } else {
                log.warn("UDP server start failed", result.cause());
            }
        });
    }

    public void tryRegister(UDPMessage message, SocketAddress sender) {
        /*
         {"type":"register","data":{"clientId":"test","username":"admin","password":"admin"}}
         */
        String clientId = message.getStringData("clientId");
        String username = message.getStringData("username");
        String password = message.getStringData("password");
        if (null == clientId || username == null || password == null) {
            sendMessageCode(sender, MessageCode.NO_AUTH_PARAM);
            return;
        }

        if (authorityService.verification(clientId, username, password)) {
            clientRepository.register(new UDPClient(clientId, sender, socket));
            sendMessageCode(sender, MessageCode.SUCCESS, "register");
        } else {
            sendMessageCode(sender, MessageCode.AUTH_FAIL);
        }

    }

    protected void sendMessageCode(SocketAddress sender, MessageCode messageCode) {
        sendMessageCode(sender, messageCode, null);
    }

    protected void sendMessageCode(SocketAddress sender, MessageCode messageCode, String type) {
        JSONObject data = new JSONObject();
        data.put("code", messageCode.getCode());
        data.put("message", messageCode.getMessage());
        data.put("type", type);
        socket.send(data.toJSONString(), sender.port(), sender.host(), res -> {
            if (res.succeeded()) {
                log.debug("发送数据到UDP[{}]完成.数据:{}", sender, data);
            } else {
                log.debug("发送数据到UDP[{}]失败.数据:{}", sender, data, res.cause());
            }
        });
    }

}
