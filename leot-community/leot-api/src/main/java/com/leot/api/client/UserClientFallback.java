package com.leot.api.client;

import com.leot.api.dto.UserDTO;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * UserClient 服务降级实现
 * 当 user-service 不可用时，返回默认值以保证系统稳定
 */
@Slf4j
@Component
public class UserClientFallback implements UserClient {

    @Override
    public BaseResponse<UserDTO> getUserById(Long id) {
        log.warn("UserClient.getUserById 服务降级, userId={}", id);
        return ResultUtil.success(null);
    }

    @Override
    public BaseResponse<List<UserDTO>> getUserListByIds(List<Long> ids) {
        log.warn("UserClient.getUserListByIds 服务降级, userIds={}", ids);
        return ResultUtil.success(Collections.emptyList());
    }
}
