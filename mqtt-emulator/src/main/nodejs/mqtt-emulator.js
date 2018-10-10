var mqtt = require("mqtt");
var ProgressBar = require("./progress-bar");
var lineReader = require('line-reader');
var fs = require("fs")
var url = require('url');

"use strict";

var argsMap = function () {
    var map = {
        "servers": "tcp://mqtt-server:1883",
        "clients": "./data/clients.txt",
        "replyData": "./data/reply.json",
        "reportData": "./data/report.json",
        "disableReport": "true",
        "skip": 0,
        "limit": 1000,
        "disableProgress": "false"
    };

    process.argv.splice(2)
        .forEach(function (t) {
            var kv = t.split("=");
            map[kv[0]] = kv.length > 1 ? kv[1] : "true"
        });
    return map;
}();


var clientsFilePath = argsMap["clients"];

var index = 0;
var skip = parseInt(argsMap['skip']);
var limit = parseInt(argsMap['limit']);
var size = 0;
var pb = new ProgressBar('启动进度', 50, argsMap["disableProgress"] === 'true');

lineReader.eachLine(clientsFilePath, function (line) {
    if (index++ >= skip) {
        doMqttConnect(line.split(":")[0], line.split(":")[1]);
        size++;
    }
    if (size >= limit) {
        return false;
    }
});

var serverUrl = url.parse(argsMap['servers']);

var connectCounter = 0;
var errorCounter = 0;
var completedCounter = 0;
var allClient = [];
var replyCounter = 0;
var replySuccessCounter = 0;

function parseJson(json) {

    return JSON.parse(json, function (key, value) {
        if (key === 'messageId') {
            return value + "";
        }
        return value;
    })
}

//创建mqtt连接
function doMqttConnect(clientId, pwd) {
    var client = mqtt.connect({
        host: serverUrl.hostname,
        port: serverUrl.port,
        protocol: serverUrl.protocol.split(":")[0],
        username: clientId,
        password: pwd,
        clientId: clientId,
        connectTimeout: 100 * 1000
    });
    var success = false;
    client.on('connect', function () {
        pb.render({completed: ++completedCounter, success: ++connectCounter, error: errorCounter, total: limit});
        success = true;
        client.on('message', function (topic, message) {
            doReply(client, parseJson(message));
        });
        allClient.push(client);
    });
    client.on("reconnect", function () {
        if (!success) {
            pb.render({completed: ++completedCounter, success: connectCounter, error: ++errorCounter, total: limit});
        }
        client.end();
    });
    client.on("disconnect", function () {
        console.log("client", clientId, "disconnect");
        client.end();
    });
    client.on("close", function () {
        console.log("client", clientId, "closed");
        client.end();
    });
    client.on('error', function (error) {
        // console.log(error.message, ":", clientId, pwd);
        if (!success) {
            pb.render({completed: ++completedCounter, success: connectCounter, error: ++errorCounter, total: limit});
        }
        client.end();
    })
}

var replyDatas = function () {
    var data = fs.readFileSync(argsMap['replyData'], 'utf8');
    return JSON.parse(data);
}();

var reportDatas = function () {
    var data = fs.readFileSync(argsMap['reportData'], 'utf8');
    return JSON.parse(data);
}();

function doReply(client, message) {
    replyCounter++;
    console.log("接收到服务器执行指令:", JSON.stringify(message));
    var operation = message.operation;
    var datas = replyDatas[operation];
    if (datas) {
        var data = datas[random(0, datas.length - 1)];

        // noinspection JSAnnotator
        function reply() {
            var newData = JSON.parse(JSON.stringify(data));
            delete newData.delay;
            newData.messageId = message.messageId;
            client.publish("reply", JSON.stringify(newData), {}, function (error) {
                replySuccessCounter++;
                console.log("回复指令[", message.messageId, "]:", JSON.stringify(newData), " ", error ? error : "");
            });
        }

        if (data.delay) {
            var time = random(data.delay[0], data.delay[1]);
            setTimeout(reply, time);
        } else {
            reply();
        }
    }
}

function random(n, m) {
    var c = m - n + 1;
    return Math.floor(Math.random() * c + n);
}

var reportCounter = 0;
var reportDataCounter = 0;


function doReport() {
    if (argsMap['disableReport'] === 'true') {
        return;
    }
    if (allClient.length === 0) {
        setTimeout(doReport, 2000);
    } else {
        var reportSuccess = 0;
        allClient.forEach(function (client) {
            var reportData = reportDatas[random(0, reportDatas.length - 1)];
            if (reportData) {
                client.publish("report", JSON.stringify(reportData), {}, function (err) {
                    if (!err) {
                        reportSuccess++;
                        reportDataCounter++;
                    }
                });
            }
        });
        reportCounter++;
        console.log("执行数据上报,总数:", allClient.length, " 成功:", reportSuccess);
        setTimeout(doReport, 10000);
    }

}

console.log("use config : \n", JSON.stringify(argsMap, null, 4));

doReport();

if (connectCounter === 0) {
    pb.render({completed: 0, success: 0, error: errorCounter, total: limit});
}
process.on('exit', function () {
    console.log("\n连接成功:", connectCounter, ",失败", errorCounter);
    console.log("上报:", reportCounter, "次,上报数据:", reportDataCounter, "次");
    console.log("接收指令:", replyCounter, "次,上报数据:", replySuccessCounter, "次");
});

process.stdin.on('readable', function () {
    var chunk = process.stdin.read();
    if (chunk && chunk.toString().trim() === 'q') {
        process.exit(1);
    }
});
