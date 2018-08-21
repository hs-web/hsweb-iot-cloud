package org.hswebframework.iot.interaction;

import org.hswebframework.web.dao.Dao;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.cloud.client.SpringCloudApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@SpringCloudApplication
@EnableFeignClients
@MapperScan(basePackages = "org.hswebframework.iot.interaction.dao", markerInterface = Dao.class)
@ComponentScan("org.hswebframework.iot")
public class InteractionApplication {

    public static void main(String[] args) {
        SpringApplication.run(InteractionApplication.class, args);
    }

}
