package com.xxl.job.api.handler.api;

import com.xxl.job.api.handler.model.ApiResult;

/**
 * Job任务需要实现的的接口
 */
public abstract class JobHandler {

    public abstract ApiResult<String> execute(String... params) throws Exception;

}
