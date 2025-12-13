package com.leot.leotcommon.utils;

import com.leot.leotcommon.GlobalReture.ErrorCode;
import com.leot.leotcommon.exception.BusinessException;

public class ThrowUtil {
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        if (condition) {
            throw new BusinessException(errorCode);
        }
    }

    public static void throwIf(boolean condition, ErrorCode errorCode, String description) {
        if (condition) {
            throw new BusinessException( errorCode.getMessage(), errorCode.getCode(),description);
        }
    }
}
