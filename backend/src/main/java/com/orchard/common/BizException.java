package com.orchard.common;

import lombok.Getter;

/** 业务异常：携带错误码 */
@Getter
public class BizException extends RuntimeException {
    private final int code;

    public BizException(String msg) {
        this(50001, msg);
    }

    public BizException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    // 常用错误码
    public static final int PARAM_ERROR = 40001;
    public static final int UNAUTHORIZED = 40002;
    public static final int FORBIDDEN = 40003;
    public static final int NOT_FOUND = 40004;
    public static final int BIZ_ERROR = 50001;
}
