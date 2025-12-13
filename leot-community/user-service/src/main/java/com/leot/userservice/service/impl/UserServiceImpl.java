package com.leot.userservice.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.constant.UserConstant;
import com.leot.leotcommon.enums.UserRoleEnum;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.userservice.domain.dto.QueryUserDTO;
import com.leot.userservice.domain.pojo.User;
import com.leot.userservice.domain.vo.AdminGetUserVO;
import com.leot.userservice.domain.vo.UserLoginVO;
import com.leot.userservice.service.UserService;
import com.leot.userservice.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* @author lx
* @description 针对表【user(用户)】的数据库操作Service实现
* @createDate 2025-11-27 20:44:18
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Override
    public String userRegister(String userName, String userPassword, String checkPassword) {
        //校验
        if (ObjUtil.hasEmpty(userName,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数有空的");
        }
        if (userName.length()<2 || userName.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过长或过断");
        }
        if (userPassword.length()< 6 || checkPassword.length()<6){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        if (!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"两次密码不一致");
        }
        //密码加密
        String MD5_Password=getMD5Password(userPassword);
        //去数据库中查是否有相同的userName
        QueryWrapper<User> queryWrapper =new QueryWrapper<>();
        queryWrapper.eq("userName",userName);
        User one = this.getOne(queryWrapper);
        if (one != null){
            throw new BusinessException(ErrorCode.FORBIDDEN,"用户名已存在");
        }
        //生成8位的账号
        Long account = RandomUtil.randomLong(10000000, 99999999);
        String userAccount = account.toString();
        //存数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserName(userName);
        user.setUserPassword(MD5_Password);

        boolean save = this.save(user);
        //用来测试账号重复
        if (!save){
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,"注册失败");
        }

        return user.getUserAccount();
    }

    @Override
    public String getMD5Password(String password) {

        return  SecureUtil.md5(UserConstant.MD5_PASSWORD_SALT+password);
    }

    /**
     * 用户登录
     * @param userName
     * @param userPassword
     * @return
     */
    @Override
    public UserLoginVO userLogin(String userName, String userPassword, HttpServletRequest request) {
        //校验
        if (ObjUtil.hasEmpty(userName,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"账号和密码不能为空");
        }
        if (userName.length()<2 || userName.length()>20){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名过长或过断");
        }
        if (userPassword.length()< 6  ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过短");
        }
        //去数据库查询  根据userName
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userName",userName);
        User one = this.getOne(queryWrapper);
        if (ObjUtil.isEmpty(one)){
            //为空 用户不存在
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户名不存在");
        }
        //看查到的与userPassword 是否匹配
        String userPassword1 = one.getUserPassword();
        String md5Password = getMD5Password(userPassword);
        if (!userPassword1.equals(md5Password)){
            throw new BusinessException(ErrorCode.ERROR_PASSWORD);
        }
        //存登录态
        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE,one);
        //记录登录态到  Sa-token

        StpUtil.login(one.getId());
        StpUtil.getSession().set(UserConstant.USER_LOGIN_STATE,one);

//        StpKit.SPACE.login(one.getId());
//        StpKit.SPACE.getSession().set(UserConstant.USER_LOGIN_STATE,one);
        return  getSafeyUser(one);
    }

    /**
     * 用户脱敏
     * @param user
     * @return
     */
    @Override
    public UserLoginVO getSafeyUser(User user) {
        if (user == null){
            return  null;
        }
        UserLoginVO userLoginVO = new UserLoginVO();

        BeanUtil.copyProperties(user,userLoginVO);
        return userLoginVO;
    }

    @Override
    public AdminGetUserVO getSageyUserAd(User user) {
        if (user == null) {
            return null;
        }
        AdminGetUserVO adminGetUserVO = new AdminGetUserVO();
        BeanUtil.copyProperties(user, adminGetUserVO);
        return adminGetUserVO;
    }


    @Override
    public List<AdminGetUserVO> getUserlistAd(List<User> userList) {
        if (userList == null || userList.isEmpty()) {
            return new ArrayList<>();
        }

        return userList.stream()
                .map(this::getSageyUserAd)
                .collect(Collectors.toList());
    }

    @Override
    public User getNowLoginUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return (User) userObj;
    }

    @Override
    public Boolean removeUserState(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (userObj==null){
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
//        StpKit.SPACE.logout(((User)userObj).getId());
        Object  partten = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
        if (partten != null){
            return false;
        }
        return true;
    }

    @Override
    public QueryWrapper<User> getQueryWrapper(QueryUserDTO queryUserDTO) {
        Long id = queryUserDTO.getId();
        String userName = queryUserDTO.getUserName();
        String userAccount = queryUserDTO.getUserAccount();
        String userProfile = queryUserDTO.getUserProfile();
        String userRole = queryUserDTO.getUserRole();
        int current = queryUserDTO.getCurrent();
        int pageSize = queryUserDTO.getPageSize();
        String sortField = queryUserDTO.getSortField();
        String sortOrder = queryUserDTO.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(ObjUtil.isNotNull(id), "id", id);
        queryWrapper.eq(StrUtil.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StrUtil.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.like(StrUtil.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StrUtil.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;

    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole().equals(UserRoleEnum.ADMIN.getValue());
    }
}




