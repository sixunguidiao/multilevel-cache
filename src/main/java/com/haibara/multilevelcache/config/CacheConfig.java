package com.haibara.multilevelcache.config;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.haibara.multilevelcache.support.MultiLevelCacheConfig;
import com.haibara.multilevelcache.support.MultiLevelCacheManager;
import com.haibara.multilevelcache.support.MultiLevelCacheMessageListener;

/**
 * 缓存配置
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-19
 */
@EnableCaching
@Configuration
public class CacheConfig {
    @Bean
    public Cache<Object, Object> caffeineCache() {
        return Caffeine.newBuilder()
                .initialCapacity(128)
                .maximumSize(1024)
                .expireAfterWrite(30, TimeUnit.SECONDS)
                .build();
    }

    @Bean
    public RedisTemplate<Object, Object> stringKeyRedisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }

    @Bean
    public MultiLevelCacheConfig cacheConfig() {
        return new MultiLevelCacheConfig();
    }

    @Bean
    public MultiLevelCacheManager cacheManager(Cache<Object, Object> caffeineCache,
            RedisTemplate<Object, Object> stringKeyRedisTemplate, MultiLevelCacheConfig config) {
        return new MultiLevelCacheManager(caffeineCache, stringKeyRedisTemplate, config);
    }

    @Bean
    public RedisMessageListenerContainer cacheMessageListenerContainer(
            RedisTemplate<Object, Object> stringKeyRedisTemplate, MultiLevelCacheManager cacheManager,
            MultiLevelCacheConfig config) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(Objects.requireNonNull(stringKeyRedisTemplate.getConnectionFactory()));
        MultiLevelCacheMessageListener listener =
                new MultiLevelCacheMessageListener(stringKeyRedisTemplate, cacheManager);
        container.addMessageListener(listener, new ChannelTopic(config.getRedisTopic()));
        return container;
    }
}