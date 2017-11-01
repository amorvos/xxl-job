package com.xxl.job.api.model;

import java.io.Serializable;

/**
 * Created by xuxueli on 17/3/2.
 */
public class HandleCallbackParam implements Serializable {
    private static final long serialVersionUID = 42L;

    private int logId;
    private ApiResult<String> executeResult;

    public HandleCallbackParam(){}
    public HandleCallbackParam(int logId, ApiResult<String> executeResult) {
        this.logId = logId;
        this.executeResult = executeResult;
    }

    public int getLogId() {
        return logId;
    }

    public void setLogId(int logId) {
        this.logId = logId;
    }

    public ApiResult<String> getExecuteResult() {
        return executeResult;
    }

    public void setExecuteResult(ApiResult<String> executeResult) {
        this.executeResult = executeResult;
    }

    @Override
    public String toString() {
        return "HandleCallbackParam{" +
                "logId=" + logId +
                ", executeResult=" + executeResult +
                '}';
    }
}
