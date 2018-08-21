# 物联网设备交互服务

物联网设备接入服务,实现 mqtt,udp方式接入.数据传输格式全部采用json.

# MQTT

MQTT地址: tcp://{host}:{port:1884}. `clientId`,`username`,`password`在平台(device-server服务)注册分配

topic: mqtt服务仅有3个topic: `execute`,`reply`,`report`

1. execute: 平台向设备发送指令的topic,设备通过订阅此topic来接收平台下发的指令.格式:
```json
{
  "messageId":"long类型的唯一id,设备在reply时必须原样返回",
  "operation":"操作类型,如:plugin",
  "parameters":{
    "type":"install",
    "pluginName":"test-plugin",
    "//":"具体格式由业务方定义"
  }
}
```

2. reply: 设备接收到平台指令,并执行完成后向此topic发送消息.格式:
```json
{
"messageId":"long类型,平台下发指令时的id",
"code":"int类型的响应码,0为成功,其他为不成功",
"data":{
    "//":"具体格式由业务方定义"
  }
}
```

3. report: 设备直接向平台上报数据时向此topic发送数据:格式:
```json
{
"messageId":"long类型的唯一id,设备自行生成唯一id",
"action":"上报数据的动作,如: sensor-temp",
"data":{
   "//":"具体格式由业务方定义"
  }
}
```

# UDP
UDP地址: {host}:{port:5010} ,支持5种操作类型:`register`,`unregister`,`ping`,`execute`,`reply`,`report`.

其中: `register`,`unregister`,`ping`,`reply`,`report` 由客户端发起,`execute`由服务器发起.字符集使用`utf-8`.

1. register 注册: 连接后首次发送消息需要先进行注册:
```json
   {
    "type":"register",
    "data":{
      "clientId":"",
      "username":"",
      "password":""
    }
   }
```
平台将返回:
```json
{
  "type":"register",
  "code":0 
}
```

2. unregister 注销: 设备关闭时应该发送次消息从平台注销:
```json
    {
     "type":"unregister"
    }
```
平台将返回:
```json
{
  "type":"unregister",
  "code":0 
}
```

3. ping 心跳: 设备应该定时发送心跳以告诉平台设备正常,如果长时间不发送此命令,平台将自动注销设备.
```json
  {
  "type":"ping"
  }
```
平台将返回:
```json
{
  "type":"ping",
  "code":0 
}
```

4. execute: 平台向设备发送执行指令:
```json
{
  "type":"execute",
  "messageId":"long类型的唯一id,设备在reply时必须原样返回",
  "operation":"操作类型,如:plugin",
  "parameters":{
    "type":"install",
    "pluginName":"test-plugin",
    "//":"具体格式由业务方定义"
  }
}
```

4. reply: 设备执行完指令,向平台返回执行结果
```json
{
"type":"reply",
"messageId":"long类型,平台下发指令时的id",
"code":"int类型的响应码,0为成功,其他为不成功",
"data":{
    "//":"具体格式由业务方定义"
  }
}
```

5. report: 设备直接向平台上报数据,格式:
```json
{
"type":"report",
"messageId":"long类型的唯一id,设备自行生成唯一id",
"action":"上报数据的动作,如: sensor-temp",
"code":0,
"data":{
   "//":"具体格式由业务方定义"
  }
}
```

错误码:

* 40301:未注册的客户端
* 40101:授权参数错误
* 40102:授权失败
* 40001:不支持的操作类型
* 40002:参数格式错误


# 平台向设备发送指令
使用http调用interaction-server服务,向在线的设备发送指令.

    POST: device/command/{clientId}
    Content-Type:application/json
    
    {
        "messageId":"long类型唯一id",
        "operation":"操作,如: plugin",
        "paramters":{
            "pluginName":"test-plugin",
            "//":"具体参数由业务方定义"
        }
    }
   
响应:

```json
    {"status":"200成功,其他失败","message":"失败原因"} 
```

# 平台接受设备提交的数据

默认会将设备返回的数据发送到kafka中. `contentType`为`application/json`

topic分别为:

1. iot.device.connect 设备连接: 

        {"clientId":"客户端ID","connectTime":"连接时间"}
        
2. iot.device.disconnect 设备断开 

        {"clientId":"客户端ID","disconnectTime":"断开连接时间"}
        
3. iot.command.reply.{operation} 设备回复命令执行结果 
        
        {
            {
            "operation":"操作",
            "messageId":"与平台下发指令时的messageId对应",
            "code":"int类型的响应码,0为成功,其他为不成功",
            "data":{
                "//":"具体格式由业务方定义"
              }
            }
        }
        
4. iot.device.report.{action} 设备上报数据

        {
            {
            "action":"动作标识,如: sensor-temp",
            "messageId":"由设备生成的messageId",
            "code":0,
            "data":{
                "//":"具体格式由业务方定义"
              }
            }
        }