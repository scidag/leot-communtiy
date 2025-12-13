package com.leot.userservice.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginDTO implements Serializable {


    private static final long serialVersionUID = 1566833700867743416L;
    private  String userName;

    private String userPassword;
}
