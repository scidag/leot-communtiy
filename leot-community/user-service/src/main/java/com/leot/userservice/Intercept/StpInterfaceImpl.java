package com.leot.userservice.Intercept;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.leot.leotcommon.constant.UserConstant;
import com.leot.userservice.domain.pojo.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
@Component
public class StpInterfaceImpl implements StpInterface {
    @Override
    public List<String> getPermissionList(Object o, String s) {
        return List.of();
    }

    @Override
    public List<String> getRoleList(Object o, String s) {
        User loginUser = (User) StpUtil.getSession().get(UserConstant.USER_LOGIN_STATE);


        // 2. 校验User是否存在，不存在返回空列表
        if (loginUser == null || loginUser.getUserRole() == null) {
            return Collections.emptyList();
        }
        return Collections.singletonList(loginUser.getUserRole());
    }
}
