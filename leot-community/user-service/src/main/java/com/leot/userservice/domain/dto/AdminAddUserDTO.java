package com.leot.userservice.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminAddUserDTO implements Serializable {


    private static final long serialVersionUID = -8989977528665963494L;

    private String userAccount;

    private String userName;

    private String userPassword;

    private String profile;


}