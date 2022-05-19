package com.haibara.multilevelcache.service;

import com.haibara.multilevelcache.po.UserPO;

/**
 * 用户服务
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-14
 */
public interface UserService {
    UserPO getUserById(Long id);
}
