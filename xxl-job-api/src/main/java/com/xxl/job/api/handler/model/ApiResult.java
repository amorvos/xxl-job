package com.xxl.job.api.handler.model;

import java.io.Serializable;

/**
 * common return
 * 
 * @author xuxueli 2015-12-4 16:32:31
 * @param <T>
 */
public class ApiResult<T> implements Serializable {
    public static final long serialVersionUID = 42L;

    public static final int SUCCESS_CODE = 200;
    public static final int FAIL_CODE = 500;
    public static final ApiResult<String> SUCCESS = new ApiResult<String>(null);
    public static final ApiResult<String> FAIL = new ApiResult<String>(FAIL_CODE, null);

    private int code;
    private String msg;
    private T content;

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

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "ReturnT [code=" + code + ", msg=" + msg + ", content=" + content + "]";
    }

}
