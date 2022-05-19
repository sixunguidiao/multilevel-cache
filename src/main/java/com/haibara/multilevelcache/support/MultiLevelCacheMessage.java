package com.haibara.multilevelcache.support;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 多级缓存消息
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-15
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MultiLevelCacheMessage {
    private String cacheName;
    private Object key;
}
