package com.leot.userservice.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterDTO implements Serializable {

    private static final long serialVersionUID = -2766610226827546588L;

    private  String userName;

    private String userPassword;

    private String checkPassword;
}
