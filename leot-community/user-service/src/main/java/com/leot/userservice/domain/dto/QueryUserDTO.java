package com.leot.userservice.domain.dto;

import com.leot.leotcommon.request.PageRequest;
import lombok.Data;

import java.io.Serializable;

@Data
public class QueryUserDTO extends PageRequest implements Serializable {

    private static final long serialVersionUID = 7229945452903693974L;

    /**
     * id
     */
    private Long id;

    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 账号
     */
    private String userAccount;

    /**
     * 简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin/ban
     */
    private String userRole;
}
