package com.haibara.multilevelcache.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.haibara.multilevelcache.po.UserPO;
import com.haibara.multilevelcache.service.UserService;

/**
 * 用户 Controller
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-14
 */
@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserPO getUserById(@PathVariable("id") Long id) {
        return userService.getUserById(id);
    }
}
