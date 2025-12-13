package com.leot.baguservice.config;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.leot.leotcommon.constant.UserConstant;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Sa-Token 权限认证接口实现
 * 用于获取当前登录用户的权限和角色信息
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    /**
     * 获取当前用户的权限列表
     * 当前系统使用角色控制，权限列表返回空
     *
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return 权限列表
     */
    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return Collections.emptyList();
    }

    /**
     * 获取当前用户的角色列表
     * 从 Sa-Token Session 中获取用户角色信息
     *
     * @param loginId 登录用户ID
     * @param loginType 登录类型
     * @return 角色列表
     */
    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        // 从 Session 中获取用户角色
        Object userRole = StpUtil.getSession().get("userRole");
        
        if (userRole == null) {
            return Collections.emptyList();
        }
        
        return Collections.singletonList(userRole.toString());
    }
}
