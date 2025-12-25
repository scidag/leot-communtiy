package com.leot.api.client;

import com.leot.api.dto.UserDTO;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 用户服务 Feign 客户端
 * 用于服务间调用 user-service 的接口
 */
@FeignClient(name = "user-service", fallback = UserClientFallback.class)
public interface UserClient {

    /**
     * 根据用户ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户信息（脱敏）
     */
    @GetMapping("/user/inner/get")
    BaseResponse<UserDTO> getUserById(@RequestParam("id") Long id);

    /**
     * 根据用户ID列表批量获取用户信息
     *
     * @param ids 用户ID列表
     * @return 用户信息列表（脱敏）
     */
    @PostMapping("/user/inner/list/ids")
    BaseResponse<List<UserDTO>> getUserListByIds(@RequestBody List<Long> ids);
}
