package org.hswebframework.iot.interaction.vertx.client;

/**
 * @author zhouhao
 * @since 1.0.0
 */
public interface ClientRepository {
    Client getClient(String idOrClientId);

    Client register(Client client);

    Client unregister(String idOrClientId);

    long total();
}
