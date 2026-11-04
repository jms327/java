package com.fenglin.springboottest.common;

import lombok.Data;

/**
 * 统一响应结构，对齐前端 api.js 的 { code, msg, data } 约定。
 * code = 0 表示成功，非 0 表示失败。
 */
@Data
public class Result<T> {

    public static final int SUCCESS = 0;
    public static final int FAIL = 1;

    private int code;
    private String msg;
    private T data;

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.code = SUCCESS;
        r.data = data;
        return r;
    }

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> fail(String msg) {
        Result<T> r = new Result<>();
        r.code = FAIL;
        r.msg = msg;
        return r;
    }
}
