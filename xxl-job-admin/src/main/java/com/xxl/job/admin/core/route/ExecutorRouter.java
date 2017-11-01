package com.xxl.job.admin.core.route;

import java.util.List;

import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/10.
 */
public abstract class ExecutorRouter {

    /**
     * route run executor
     *
     * @param triggerParam
     * @param addressList
     * @return ApiResult.content: final address
     */
    public abstract ApiResult<String> routeRun(TriggerParam triggerParam, List<String> addressList);

}
