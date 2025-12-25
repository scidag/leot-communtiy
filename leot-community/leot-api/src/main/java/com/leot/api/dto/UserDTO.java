package com.leot.api.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户数据传输对象（脱敏）
 * 用于服务间传输用户信息，不包含敏感字段
 */
@Data
public class UserDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户账号
     */
    private String userAccount;

    /**
     * 用户头像URL
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;
}
