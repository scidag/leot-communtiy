package com.leot.userservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.leot.userservice.domain.dto.QueryUserDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.leot.userservice.domain.pojo.User;
import com.leot.userservice.domain.vo.AdminGetUserVO;
import com.leot.userservice.domain.vo.UserLoginVO;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
* @author lx
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-11-27 20:44:18
*/
public interface UserService extends IService<User> {

    /**
     * 注册
     * @param userName
     * @param userPassword
     * @param checkPassword
     * @return
     */
    String userRegister(String userName,String userPassword, String checkPassword);
    /**
     * 密码加密
     */
    String getMD5Password(String password);

    /**
     * 登录
     * @param userName
     * @param userPassword
     * @return
     */
    UserLoginVO userLogin(String userName, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     */
    UserLoginVO getSafeyUser(User user);

    /**
     * 用户脱敏 管理员查询
     */
    AdminGetUserVO getSageyUserAd(User user);

/**
 * Retrieves a list of AdminGetUserVO objects based on the provided list of User objects.
 * This method is likely used to convert or transform user data into a format suitable for admin display.
 *
 * @param userList The list of User objects to be converted/transformed
 * @return List<AdminGetUserVO> A list of view objects containing user information in admin-friendly format
 */
    List<AdminGetUserVO> getUserlistAd(List<User> userList);
    /**
     * 获取当前用户
     */
    User getNowLoginUser(HttpServletRequest request);

    /**
     * 退出登录
     *
     * @param request
     * @return
     */
    Boolean removeUserState(HttpServletRequest request);

    /**
     * 得到queryWarrper
     */
    QueryWrapper<User> getQueryWrapper(QueryUserDTO queryUserDTO);

    boolean isAdmin(User user);

}
