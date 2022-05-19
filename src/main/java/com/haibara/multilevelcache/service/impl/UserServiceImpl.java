package com.haibara.multilevelcache.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.haibara.multilevelcache.mapper.UserMapper;
import com.haibara.multilevelcache.po.UserPO;
import com.haibara.multilevelcache.service.UserService;

/**
 * 用户服务实现类
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-14
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;

    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    @Override
    public UserPO getUserById(Long id) {
        return userMapper.getUserById(id);
    }
}
