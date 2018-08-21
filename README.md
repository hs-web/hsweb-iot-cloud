# 开源物联网管理平台

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)

## 目的
提供一个开源的物联网管理平台,支持各种设备接入,并完成数据上报,分析,处理等自定义功能.

### 技术栈
1. java8,maven3
2. [spring-boot](https://spring.io/projects/spring-boot): 整合各种组件
3. [spring-cloud](https://spring.io/projects/spring-cloud): 对微服务提供支持
4. [hsweb-framework](https://github.com/hs-web/hsweb-framework): 基础业务框架
5. [vertx](https://vertx.io/): 物联网通信(mqtt,udp)
6. [redisson](https://github.com/redisson/redisson): redis客户端
7. [spring-cloud-stream](https://cloud.spring.io/spring-cloud-stream/): 事件驱动
8. [kafka](http://kafka.apache.org/): 消息中间件
9. [docker](https://www.docker.com/): 快速环境搭建,持续交付

在使用本项目之前,你应该对以上技术有所了解.

### 模块介绍

     ---------hsweb-iot-cloud
     -------------docker                    # 一些docker脚本
     ---------------dev-env                 # 启动开发环境需要的外部服务(redis,kafka,zookeeper等)
     -------------eureka-server             # 服务注册中心
     -------------gateway-server            # 基于zuul的网关服务
     -------------iot-components            # 通用组件
     ------------------iot-authorization    # 权限集成
     ------------------iot-cloud-stream     # spring-cloud-stream集成
     ------------------iot-logging          # 访问日志,系统日志集成
     ------------------iot-redis            # redis集成
     ------------------server-dependencies  # 微服务通用依赖
     -------------user-server               # 用户服务
     -------------interaction-server        # 物联网设备交互服务
     
约定: 所有微服务以`-server`为后缀,微服务禁止依赖其他微服务,只能依赖`iot-components`内的通用依赖.
微服务间使用`FeignClient`或者`spring cloud stream` 进行通信.


### 使用

本项目使用了redis,kafka,zookeeper.因此在启动项目之前需要先安装并启动相应服务.

以linux为例:

1. 下载源码
    
        git clone https://github.com/hs-web/hsweb-iot-cloud.git

2. 执行启动开发环境脚本,将会使用docker安装相应服务.(自行安装docker)

        ./start-dev-env.sh
        
未提供windows下的安装脚本,windows下请自行安装相关服务:`redis`,`zookeeper`,`kafka`.

3. 依次启动服务
       
        eureka-server,gateway-server,user-server....
 
4. 服务启动成功后,浏览器访问: http://localhost:8000 ,用户名:admin 密码: admin

5. 数据库,项目默认使用`h2`数据库,可自行修改`application.yml`配置更改数据库,目前支持: h2,mysql,oracle数据库.
系统首次启动将会自动初始化数据库,无需导入数据库脚本.

遇到问题? 可以加入QQ群:`515649185`,
或者使用[issues](https://github.com/hs-web/hsweb-iot-cloud/issues/new)提问.

### 贡献
目前缺前端大佬一名, 要求: 
1. 有开源精神,原意无偿并长期献身开源项目.
2. 对`hsweb`感兴趣.
3. 有能力使用主流前端框架重写现有功能页面.
4. 加入QQ群:`515649185`(备注:`hsweb-iot-cloud`) @群主.

### License
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg?style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0.html)
