package com.xxl.job.admin.core.route.strategy;

import java.util.List;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFirst extends ExecutorRouter {

    public String route(int jobId, List<String> addressList) {
        return addressList.get(0);
    }

    @Override
    public ApiResult<String> routeRun(TriggerParam triggerParam, List<String> addressList) {

        // address
        String address = route(triggerParam.getJobId(), addressList);

        // run executor
        ApiResult<String> runResult = XxlJobTrigger.runExecutor(triggerParam, address);
        runResult.setContent(address);
        return runResult;
    }
}
