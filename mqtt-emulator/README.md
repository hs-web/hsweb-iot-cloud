# MQTT客户端模拟器

用户模拟多个mqtt客户端,以及指令接收,反馈,数据上报等功能.

# 准备数据

程序运行需要数据文件:

1. data/clients.txt ,要连接的客户端列表,一个客户端一行,格式: clientId:password
2. data/reply.json , 客户端收到服务器指令是做出回复的数据.
```js
    {
       //(对应服务端指令中的operation)
      "PLUGIN_INSTALL": 
      [ //如果集合中存在多条数据,随机一条进行回复
        {
        //(延迟回复,随机0到10000毫秒)
         "delay": [
                    0,
                    10000
          ],
          //自定义回复格式
          "operation": "plugin",
          "code": 0
        }
      ]
    }
```
3. data/report.json ,客户端定时上报的数据

随机抽取集合中的数据进行上报
```json
    [
      {
        "action": "temp-test",
        "data": {
          "key": 1,
          "key2": "2"
        }
      },
      {
        "action": "temp-test",
        "data": {
          "key": 10000,
          "key2": "value"
        }
      }
    ]
```

# 打包,运行
```bash
    $ ./build.sh
    构建成功
    $ cd bin
    $ java -jar mqtt-emulator.jar servers=tcp://127.0.0.1:1883 
```

# docker
```bash
   $ docker run -it --rm hsweb/iot-cloud-mqtt-benchmark servers=tcp://mqtt-host:1883
```

使用自定义数据
```bash
   $ docker run -v my-data-dir:/app/data -it --rm hsweb/iot-cloud-mqtt-benchmark servers=tcp://mqtt-host:1883
```

# 参数

    servers=tcp://host:port    # mqtt的服务器地址
    clients=./data/clients.txt # 需要连接的客户端列表文件
    autoReconnect=*true/false  # 是否自动重连
    skip=0                     # 从clients.txt的第几行开始
    limit=2147483647           # 最大客户端数量
    disableReport=true/*false  # 是否定时上报数据,上报数据的文件为./data/report.json中的随机内容

