package com.xxl.job.api.api;

import com.xxl.job.api.model.ApiResult;

/**
 * Job任务需要实现的的接口
 */
public abstract class JobHandler {

    public abstract ApiResult execute(String... params) throws Exception;

}
