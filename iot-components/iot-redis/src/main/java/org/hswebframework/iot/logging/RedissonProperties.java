package org.hswebframework.iot.logging;

import lombok.Getter;
import lombok.Setter;
import org.redisson.config.Config;

/**
 * @author zhouhao
 * @since 1.1.0
 */
@Getter
@Setter
public class RedissonProperties {
    private String[] hosts;

    private int database = 0;

    private String password;

    private int connectionPoolSize = 128;

    private int connectionTimeout = 10000;

    private int timeout = 10000;

    private Type type = Type.single;

    public Config toConfig(RedissonProperties defaultProperties) {
        if (this.hosts == null) {
            this.hosts = defaultProperties.hosts;
        }
        if (this.password == null) {
            this.password = defaultProperties.password;
        }
        return type.parse(this);
    }

    public enum Type {
        single {
            @Override
            Config parse(RedissonProperties properties) {
                Config config = new Config();
                config.useSingleServer()
                        .setAddress(properties.getHosts()[0])
                        .setPassword(properties.getPassword())
                        .setConnectionPoolSize(properties.getConnectionPoolSize())
                        .setConnectTimeout(properties.getConnectionTimeout())
                        .setTimeout(properties.timeout)
                        .setDatabase(properties.getDatabase());
                return config;
            }
        }, cluster {
            @Override
            Config parse(RedissonProperties properties) {
                Config config = new Config();
                String[] hosts = properties.getHosts();
                String master = hosts[0];
                String[] slave = new String[hosts.length - 1];
                System.arraycopy(hosts, 1, slave, 0, slave.length);
                config.useMasterSlaveServers()
                        .setMasterAddress(master)
                        .addSlaveAddress(hosts)
                        .setMasterConnectionPoolSize(properties.getConnectionPoolSize())
                        .setSlaveConnectionPoolSize(properties.getConnectionPoolSize())
                        .setConnectTimeout(properties.getConnectionTimeout())
                        .setTimeout(properties.timeout)
                        .setPassword(properties.getPassword())
                        .setDatabase(properties.getDatabase());
                return config;
            }
        };

        abstract Config parse(RedissonProperties properties);
    }
}
