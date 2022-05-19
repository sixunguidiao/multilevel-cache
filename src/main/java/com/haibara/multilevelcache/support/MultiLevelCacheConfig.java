package com.haibara.multilevelcache.support;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Data;

/**
 * 多级缓存管理器配置
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-14
 */
@Data
public class MultiLevelCacheConfig {
    /**
     * cacheName 集合
     */
    private Set<String> cacheNameSet = new HashSet<>();

    /**
     * 是否允许动态创建 Cache
     */
    private boolean dynamic = true;

    /**
     * 是否允许缓存空值
     */
    private boolean allowNullValues = false;

    /**
     * 是否开启 Caffeine
     */
    private boolean enableCaffeine = true;

    /**
     * 是否开启 Redis
     */
    private boolean enableRedis = true;

    /**
     * Redis 的全局默认过期时间
     */
    private Duration defaultRedisExpire = Duration.ofMinutes(1L);

    /**
     * 每个 cacheName 的 Redis 的过期时间，若指定了某个 cacheName 的过期时间，则会覆盖 defaultRedisExpire
     */
    private Map<String, Duration> cacheNameRedisExpireMap = new HashMap<>();

    /**
     * 缓存更新时通知其他节点的 topic
     */
    private String redisTopic = "redis_caffeine_topic";
}
