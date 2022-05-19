package com.haibara.multilevelcache.support;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;

import lombok.extern.slf4j.Slf4j;

/**
 * 多级缓存消息监听器
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-15
 */
@Slf4j
public class MultiLevelCacheMessageListener implements MessageListener {
    private final RedisTemplate<Object, Object> redisTemplate;
    private final MultiLevelCacheManager cacheManager;

    public MultiLevelCacheMessageListener(RedisTemplate<Object, Object> redisTemplate,
            MultiLevelCacheManager cacheManager) {
        this.redisTemplate = redisTemplate;
        this.cacheManager = cacheManager;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        MultiLevelCacheMessage cacheMessage = (MultiLevelCacheMessage) redisTemplate.getValueSerializer().deserialize(message.getBody());
        if (cacheMessage == null) {
            return;
        }
        log.debug("Receive a redis message! cacheName = {}, key = {}", cacheMessage.getCacheName(), cacheMessage.getKey());
        cacheManager.clearLocal(cacheMessage.getCacheName(), cacheMessage.getKey());
    }
}
