package com.leot.leotcommon.enums;

import cn.hutool.core.util.ObjUtil;
import lombok.Getter;

import java.util.HashMap;

@Getter
public enum UserRoleEnum {

    USER("用户", "user"),
    ADMIN("管理员", "admin");
    private final String text;
    private final String value;

    UserRoleEnum(String text, String value) {
        this.text = text;
        this.value = value;
    }

    /**
     * 根据value 获取枚举
     */
    public static UserRoleEnum getEnumByValue(String value) {
        if (ObjUtil.isEmpty(value)) {
            return null;
        }

        HashMap<String, UserRoleEnum> map = new HashMap<>();
        for (UserRoleEnum userRoleEnum : UserRoleEnum.values()) {
            map.put(userRoleEnum.value, userRoleEnum);
        }
        UserRoleEnum ure = map.get(value);
        if (ObjUtil.isEmpty(ure)) {
            return null;
        }
        return ure;
    }
}