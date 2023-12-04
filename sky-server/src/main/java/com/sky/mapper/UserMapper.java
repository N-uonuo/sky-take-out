package com.sky.mapper;


import com.sky.entity.User;
import io.swagger.models.auth.In;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;


@Mapper
public interface UserMapper{

    /**
     * 根据openid查询用户信息
     * @param openid
     * @return
     */
    @Select("select * from sky_take_out.user where openid=#{openid}")
    User getByOpenid(String openid);

    /**
     * 新增用户
     * @param user
     */
    void insert(User user);

    //根据动态数量查询用户数量
    Integer countByMap(Map map);



}
