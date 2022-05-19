package com.haibara.multilevelcache.support;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存管理器
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-13
 */
@Slf4j
public class MultiLevelCacheManager implements CacheManager {
    private final ConcurrentMap<String, Cache> cacheMap = new ConcurrentHashMap<>(16);
    private final com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache;
    private final RedisTemplate<Object, Object> stringKeyRedisTemplate;
    private final Set<String> cacheNameSet;
    private final boolean dynamic;
    private final MultiLevelCacheConfig multiLevelCacheConfig;

    public MultiLevelCacheManager(com.github.benmanes.caffeine.cache.Cache<Object, Object> caffeineCache,
            RedisTemplate<Object, Object> stringKeyRedisTemplate, MultiLevelCacheConfig multiLevelCacheConfig) {
        super();
        this.caffeineCache = caffeineCache;
        this.stringKeyRedisTemplate = stringKeyRedisTemplate;
        this.cacheNameSet = multiLevelCacheConfig.getCacheNameSet();
        this.dynamic = multiLevelCacheConfig.isDynamic();
        this.multiLevelCacheConfig = multiLevelCacheConfig;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = cacheMap.get(name);
        if (cache != null) {
            return cache;
        }
        if (!cacheNameSet.contains(name) && !dynamic) {
            return null;
        }
        cache = createMultiLevelCache(name);
        log.debug("Create cache! cacheName = {}", name);
        cacheMap.put(name, cache);
        return cache;
    }

    @Override
    public Collection<String> getCacheNames() {
        return Collections.unmodifiableSet(cacheMap.keySet());
    }

    public void clearLocal(String cacheName, Object key) {
        Cache cache = cacheMap.get(cacheName);
        if (cache == null) {
            return;
        }
        MultiLevelCache multiLevelCache = (MultiLevelCache) cache;
        multiLevelCache.clearLocal(key);
    }

    private Cache createMultiLevelCache(String name) {
        return new MultiLevelCache(name, caffeineCache, stringKeyRedisTemplate, multiLevelCacheConfig);
    }
}