package com.xxl.job.core.biz;

import java.util.List;

import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.HandleCallbackParam;
import com.xxl.job.api.model.RegistryParam;

/**
 * @author xuxueli 2017-07-27 21:52:49
 */
public interface AdminBiz {

    static final String MAPPING = "/api";

    /**
     * callback
     *
     * @param callbackParamList
     * @return
     */
    ApiResult callback(List<HandleCallbackParam> callbackParamList);

    /**
     * registry
     *
     * @param registryParam
     * @return
     */
    ApiResult registry(RegistryParam registryParam);

    /**
     * registry remove
     *
     * @param registryParam
     * @return
     */
    ApiResult registryRemove(RegistryParam registryParam);

    /**
     * trigger job for once
     *
     * @param jobId
     * @return
     */
    ApiResult triggerJob(int jobId);

}
