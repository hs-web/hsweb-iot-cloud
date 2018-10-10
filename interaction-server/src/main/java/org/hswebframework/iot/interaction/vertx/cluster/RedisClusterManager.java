package org.hswebframework.iot.interaction.vertx.cluster;

import io.vertx.core.*;
import io.vertx.core.impl.ContextImpl;
import io.vertx.core.shareddata.AsyncMap;
import io.vertx.core.shareddata.Counter;
import io.vertx.core.shareddata.Lock;
import io.vertx.core.spi.cluster.AsyncMultiMap;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.core.spi.cluster.NodeListener;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RSet;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zhouhao
 * @since 1.0.0
 */
@Slf4j
public class RedisClusterManager implements ClusterManager {

    private RedissonClient redissonClient;

    private String nodeId;

    private RSet<String> allNode;

    private volatile boolean active;

    private Vertx vertx;

    public RedisClusterManager(RedissonClient client, String nodeId) {
        this.allNode = client.getSet("vertx-cluster-all-node", StringCodec.INSTANCE);
        this.redissonClient = client;
        this.active = true;
        this.nodeId = nodeId;
    }

    @Override
    public void setVertx(Vertx vertx) {
        this.vertx = vertx;
    }

    @Override
    public <K, V> void getAsyncMultiMap(String name, Handler<AsyncResult<AsyncMultiMap<K, V>>> asyncResultHandler) {
        asyncResultHandler.handle(Future.succeededFuture(new RedisAsyncMultiMap<>(redissonClient.getListMultimap(name))));
    }

    @Override
    public <K, V> void getAsyncMap(String name, Handler<AsyncResult<AsyncMap<K, V>>> asyncResultHandler) {
        asyncResultHandler.handle(Future.succeededFuture(new RedisAsyncMap<>(redissonClient.getMapCache(name))));
    }

    @Override
    public <K, V> Map<K, V> getSyncMap(String name) {
        return redissonClient.getMap(name);
    }

    @Override
    public void getLockWithTimeout(String name, long timeout, Handler<AsyncResult<Lock>> resultHandler) {
        ContextImpl context = (ContextImpl) vertx.getOrCreateContext();
        context.executeBlocking(() -> {
            java.util.concurrent.locks.Lock lock = redissonClient.getLock(name);
            try {
                if (lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    return lock::unlock;
                } else {
                    throw new VertxException("Timed out waiting to get lock " + name);
                }
            } catch (Exception e) {
                throw new VertxException("get lock" + name + " error", e);
            }
        }, resultHandler);
    }

    @Override
    public void getCounter(String name, Handler<AsyncResult<Counter>> resultHandler) {
        resultHandler.handle(Future.succeededFuture(new RedisCounter(redissonClient.getAtomicLong(name))));
    }

    @Override
    public String getNodeID() {
        return nodeId;
    }

    @Override
    public List<String> getNodes() {
        return new ArrayList<>(allNode);
    }

    @Override
    public void nodeListener(NodeListener listener) {
        redissonClient.<NodeChangedEvent>getTopic("vertx-cluster-node-listener")
                .addListener((channel, msg) -> {
                    if (nodeId.equals(msg.getNodeId())) {
                        return;
                    }
                    log.info("vertx-node-{}-{}", msg.getNodeId(), msg.getType());
                    if (msg.getType() == NodeChangedEvent.EventType.leave) {
                        listener.nodeLeft(msg.getNodeId());
                    } else if (msg.getType() == NodeChangedEvent.EventType.join) {
                        listener.nodeAdded(msg.getNodeId());
                    }
                });
    }

    @Override
    public void join(Handler<AsyncResult<Void>> resultHandler) {
        allNode.add(nodeId);
        redissonClient.<NodeChangedEvent>getTopic("vertx-cluster-node-listener")
                .publish(new NodeChangedEvent(NodeChangedEvent.EventType.join, nodeId));
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public void leave(Handler<AsyncResult<Void>> resultHandler) {
        allNode.remove(nodeId);
        redissonClient.<NodeChangedEvent>getTopic("vertx-cluster-node-listener")
                .publish(new NodeChangedEvent(NodeChangedEvent.EventType.leave, nodeId));
        resultHandler.handle(Future.succeededFuture());
    }

    @Override
    public boolean isActive() {
        return active;
    }


}
