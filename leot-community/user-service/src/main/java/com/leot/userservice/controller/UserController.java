package com.leot.userservice.controller;

import cn.dev33.satoken.annotation.SaCheckRole;
import cn.hutool.core.util.ObjUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.leotcommon.exception.BusinessException;
import com.leot.leotcommon.request.DeleteRequest;
import com.leot.userservice.domain.dto.*;
import com.leot.userservice.domain.pojo.User;
import com.leot.userservice.domain.vo.AdminGetUserVO;
import com.leot.userservice.domain.vo.UserLoginVO;
import com.leot.userservice.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    public BaseResponse<String> UserRegister(@RequestBody UserRegisterDTO userRegisterDTO){
        if (ObjUtil.isEmpty(userRegisterDTO)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数不能为空");
        }
        String userName = userRegisterDTO.getUserName();
        String userPassword = userRegisterDTO.getUserPassword();
        String checkPassword = userRegisterDTO.getCheckPassword();

        String account = userService.userRegister(userName, userPassword, checkPassword);
        return ResultUtil.success(account);
    }
    @PostMapping("/login")
    public BaseResponse<UserLoginVO> UserLogin(@RequestBody UserLoginDTO userLoginDTO, HttpServletRequest request){
        if (ObjUtil.isEmpty(userLoginDTO)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数不能为空");
        }
        String userName = userLoginDTO.getUserName();
        String userPassword = userLoginDTO.getUserPassword();
        UserLoginVO userLoginVO= userService.userLogin(userName,userPassword,request);
        return ResultUtil.success(userLoginVO);
    }
    @GetMapping("/get/loginuser")
    public BaseResponse<UserLoginVO> getCurrentUser(HttpServletRequest request){
        User nowLoginUser = userService.getNowLoginUser(request);
        UserLoginVO userLoginVO = userService.getSafeyUser(nowLoginUser);
        return ResultUtil.success(userLoginVO);
    }
    @PostMapping("/logout")
    public BaseResponse<Boolean> removeUserLoginState(HttpServletRequest request){
        Boolean result= userService.removeUserState(request);
        return ResultUtil.success(result);
    }

    // 用户管理功能具体可以拆分为：

    //-   【管理员】创建用户
    @PostMapping("/add")
    @SaCheckRole("admin")
    public BaseResponse<String> createUser(@RequestBody AdminAddUserDTO adminAddUserDTO) {
        if (adminAddUserDTO == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = new User();
        user.setUserAccount(adminAddUserDTO.getUserAccount());
        user.setUserName(adminAddUserDTO.getUserName());
        user.setUserProfile(adminAddUserDTO.getProfile());
        user.setUserPassword(adminAddUserDTO.getUserPassword());
        boolean save = userService.save(user);
        if (!save) {
            throw new BusinessException(ErrorCode.SYSTEM_DATABASE_ERROR);
        }
        return ResultUtil.success(user.getUserAccount());
    }

    //- 【管理员】根据 id 删除用户
    @DeleteMapping("/delete")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> DeleteUserByID(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        Long id = deleteRequest.getId();
        boolean b = userService.removeById(id);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_DATABASE_ERROR);
        }
        return ResultUtil.success(b);
    }

    //- 【管理员】更新用户
    @PutMapping("/updateuserinfo")
    @SaCheckRole("admin")
    public BaseResponse<Boolean> updateUserInfo(@RequestBody AdminUpdateUserDTO adminUpdateUserDTO) {
        if (ObjUtil.isEmpty(adminUpdateUserDTO)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User user = new User();
        BeanUtils.copyProperties(adminUpdateUserDTO, user);
        boolean b = userService.updateById(user);
        if (!b) {
            throw new BusinessException(ErrorCode.SYSTEM_DATABASE_ERROR);
        }
        return ResultUtil.success(b);
    }
    //- 【管理员】分页获取用户列表（需要脱敏）

    @PostMapping("/list")
    @SaCheckRole("admin")
    public BaseResponse<Page<AdminGetUserVO>> getList(@RequestBody QueryUserDTO queryUserDTO) {
        if (ObjUtil.isEmpty(queryUserDTO)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        long current = queryUserDTO.getCurrent();
        long pageSize = queryUserDTO.getPageSize();

        Page<User> page = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(queryUserDTO));
        Page<AdminGetUserVO> adminGetUserVOPage1 =new Page<>(current,pageSize,page.getTotal());
        List<AdminGetUserVO> adminGetUserVOS =userService.getUserlistAd(page.getRecords());
        adminGetUserVOPage1.setRecords(adminGetUserVOS);
        return  ResultUtil.success(adminGetUserVOPage1);
    }

    //- 【管理员】根据 id 获取用户（未脱敏）
    @GetMapping("/getone")
    @SaCheckRole("admin")
    public BaseResponse<User> getUserById(@PathVariable Long id) {
        if (id < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.SYSTEM_DATABASE_ERROR);
        }
        return ResultUtil.success(user);
    }

    //- 根据 id 获取用户（脱敏）
    @PostMapping("/getone/safe")
    public BaseResponse<AdminGetUserVO> getUserById(@RequestBody QueryUserDTO queryUserDTO) {
        if (ObjUtil.isEmpty(queryUserDTO)) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        User byId = userService.getById(queryUserDTO.getId());
        AdminGetUserVO sageyUserAd = userService.getSageyUserAd(byId);
        return ResultUtil.success(sageyUserAd);
    }
}
