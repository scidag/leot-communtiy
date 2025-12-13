package com.leot.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.leot.userservice.domain.pojo.User;
import org.apache.ibatis.annotations.Mapper;

/**
* @author lx
* @description 针对表【user(用户)】的数据库操作Mapper
* @createDate 2025-11-27 20:44:18
* @Entity com.leot.userservice.domain.pojo.User
*/
@Mapper
public interface UserMapper extends BaseMapper<User> {

}




