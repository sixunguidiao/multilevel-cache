package com.haibara.multilevelcache.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.haibara.multilevelcache.po.UserPO;

/**
 * 用户 Mapper
 *
 * @author zhaoqiang05 <zhaoqiang05@kuaishou.com>
 * Created on 2022-05-06
 */
@Mapper
public interface UserMapper {
    @Select("SELECT * FROM user WHERE id = #{id}")
    UserPO getUserById(@Param("id") Long id);
}