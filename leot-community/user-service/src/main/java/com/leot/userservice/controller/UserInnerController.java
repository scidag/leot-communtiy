package com.leot.userservice.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjUtil;
import com.leot.api.dto.UserDTO;
import com.leot.leotcommon.GlobalReture.BaseResponse;
import com.leot.leotcommon.GlobalReture.ResultUtil;
import com.leot.userservice.domain.pojo.User;
import com.leot.userservice.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户内部接口控制器
 * 仅供服务间调用，不对外暴露
 */
@RestController
@RequestMapping("/user/inner")
public class UserInnerController {

    @Resource
    private UserService userService;

    /**
     * 根据用户ID获取用户信息（脱敏）
     *
     * @param id 用户ID
     * @return 用户信息
     */
    @GetMapping("/get")
    public BaseResponse<UserDTO> getUserById(@RequestParam("id") Long id) {
        if (ObjUtil.isEmpty(id)) {
            return ResultUtil.success(null);
        }
        User user = userService.getById(id);
        if (user == null) {
            return ResultUtil.success(null);
        }
        return ResultUtil.success(userService.convertToDTO(user));
    }

    /**
     * 根据用户ID列表批量获取用户信息（脱敏）
     *
     * @param ids 用户ID列表
     * @return 用户信息列表
     */
    @PostMapping("/list/ids")
    public BaseResponse<List<UserDTO>> getUserListByIds(@RequestBody List<Long> ids) {
        if (CollUtil.isEmpty(ids)) {
            return ResultUtil.success(Collections.emptyList());
        }
        List<User> users = userService.listByIds(ids);
        if (CollUtil.isEmpty(users)) {
            return ResultUtil.success(Collections.emptyList());
        }
        List<UserDTO> userDTOList = users.stream()
                .map(userService::convertToDTO)
                .collect(Collectors.toList());
        return ResultUtil.success(userDTOList);
    }
}
