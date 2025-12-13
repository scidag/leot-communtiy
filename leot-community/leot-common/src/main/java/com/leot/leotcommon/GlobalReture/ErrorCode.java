package com.leot.leotcommon.GlobalReture;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NO_FOUND(40401, "数据库中没有该数据", ""),
    DATABASE_OPERATION_ERROR(50302, "数据库操作错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    OPERATION_ERROR(40402, "没有找到对象", ""),
    FORBIDDEN(40301, "禁止操作", ""),
    ERROR_PASSWORD(40201, "密码错误", ""),
    EXIST_ERROR(50020, "已存在", ""),
    NET_ERROR(50030, "网络错误", ""),
    SYSTEM_ERROR(50000, "系统内部异常", ""),
    SYSTEM_DATABASE_ERROR(50300, "数据库异常", "");
    private final int code;
    /**
     * 状态码信息
     */
    private final String message;
    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


    public String getDescription() {
        return description;
    }
}
