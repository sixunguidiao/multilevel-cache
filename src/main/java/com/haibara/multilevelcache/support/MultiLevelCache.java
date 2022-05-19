package com.haibara.multilevelcache.support;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.cache.support.AbstractValueAdaptingCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-13
 */
@Slf4j
public class MultiLevelCache extends AbstractValueAdaptingCache {
    private static final String JOINER = "::";

    private final String name;
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
    private final RedisTemplate<Object, Object> stringKeyRedisTemplate;
    private final Map<String, ReentrantLock> keyLockMap = new ConcurrentHashMap<>(16);
    private final boolean enableCaffeine;
    private final boolean enableRedis;
    private final Duration defaultRedisExpire;
    private final Map<String, Duration> cacheNameRedisExpireMap;
    private final String topic;

    public MultiLevelCache(String name, com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache,
            RedisTemplate<Object, Object> stringKeyRedisTemplate, MultiLevelCacheConfig multiLevelCacheConfig) {
        super(multiLevelCacheConfig.isAllowNullValues());
        this.name = name;
        this.caffeineCache = caffeineCache;
        this.stringKeyRedisTemplate = stringKeyRedisTemplate;
        this.enableCaffeine = multiLevelCacheConfig.isEnableCaffeine();
        this.enableRedis = multiLevelCacheConfig.isEnableRedis();
        this.defaultRedisExpire = multiLevelCacheConfig.getDefaultRedisExpire();
        this.cacheNameRedisExpireMap = multiLevelCacheConfig.getCacheNameRedisExpireMap();
        this.topic = multiLevelCacheConfig.getRedisTopic();
    }

    @Override
    protected Object lookup(Object key) {
        Object redisKey = getRedisKey(key);

        // 从 Caffeine 中获取 value
        Object value = caffeineCache.getIfPresent(key);
        if (value != null) {
            log.debug("Get cache from caffeine! key = {}", key);
            return value;
        }

        // 从 Redis 中获取 value，如果获取成功，将 value 放入 Caffeine 中
        value = stringKeyRedisTemplate.opsForValue().get(redisKey);
        if (value != null) {
            log.debug("Get cache from redis! key = {}", redisKey);
            if (enableCaffeine) {
                caffeineCache.put(key, value);
            }
        }

        return value;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Object getNativeCache() {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value = lookup(key);
        if (value != null) {
            return (T) value;
        }
        ReentrantLock lock = keyLockMap.computeIfAbsent(String.valueOf(key), k -> {
            log.debug("Create lock for key! key = {}", k);
            return new ReentrantLock();
        });
        try {
            lock.lock();
            value = lookup(key);
            if (value != null) {
                return (T) value;
            }
            value = valueLoader.call();
            Object storeValue = toStoreValue(value);
            put(key, storeValue);
            return (T) value;
        } catch (Exception e) {
            throw new ValueRetrievalException(key, valueLoader, e);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(Object key, Object value) {
        if (!super.isAllowNullValues() && value == null) {
            this.evict(key);
            return;
        }
        value = toStoreValue(value);
        Duration expire = getExpire();
        if (enableRedis) {
            stringKeyRedisTemplate.opsForValue().set(getRedisKey(key), value, expire);
            push(new MultiLevelCacheMessage(name, key));
        }
        if (enableCaffeine) {
            caffeineCache.put(key, value);
        }

    }

    @Override
    public void evict(Object key) {
        stringKeyRedisTemplate.delete(getRedisKey(key));
        push(new MultiLevelCacheMessage(name, key));
        caffeineCache.invalidate(key);
    }

    @Override
    public void clear() {
        Set<Object> redisKeySet = stringKeyRedisTemplate.keys(name.concat(JOINER).concat("*"));
        if (!CollectionUtils.isEmpty(redisKeySet)) {
            stringKeyRedisTemplate.delete(redisKeySet);
            push(new MultiLevelCacheMessage(name, null));
        }
        caffeineCache.invalidateAll();
    }

    public void clearLocal(Object key) {
        log.debug("Clear caffeine cache!key = {}", key);
        if (key == null) {
            caffeineCache.invalidateAll();
        } else {
            caffeineCache.invalidate(key);
        }
    }

    private Object getRedisKey(Object key) {
        return name.concat(JOINER).concat(String.valueOf(key));
    }

    private Duration getExpire() {
        Duration cacheNameExpire = cacheNameRedisExpireMap.get(name);
        return cacheNameExpire == null ? defaultRedisExpire : cacheNameExpire;
    }

    private void push(MultiLevelCacheMessage message) {
        stringKeyRedisTemplate.convertAndSend(topic, message);
    }
}