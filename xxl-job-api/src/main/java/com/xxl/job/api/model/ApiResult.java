package com.xxl.job.api.model;

import java.io.Serializable;

/**
 * common return
 * 
 * @author xuxueli 2015-12-4 16:32:31
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class ApiResult<T> implements Serializable {
    public static final long serialVersionUID = 42L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final ApiResult<String> SUCCESS = new ApiResult<String>(null);
    public static final ApiResult<String> FAIL = new ApiResult<String>(FAIL_CODE, null);

    private int code;
    private String msg;
    private Object content;

    public ApiResult() {
    }

    public ApiResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public ApiResult(T content) {
        this.code = SUCCESS_CODE;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public T getContent() {
        return (T) content;
    }

    public String getMsg() {
        return msg;
    }

    public ApiResult setCode(int code) {
        this.code = code;
        return this;
    }

    public ApiResult setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public ApiResult setContent(Object content) {
        this.content = content;
        return this;
    }

    @Override
    public String toString() {
        return "ReturnT [code=" + code + ", msg=" + msg + ", content=" + content + "]";
    }

}
