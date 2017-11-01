package com.xxl.job.admin.core.route.strategy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.TriggerParam;
import com.xxl.job.core.biz.ExecutorBiz;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteFailover extends ExecutorRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRouteFailover.class);

    public String route(int jobId, ArrayList<String> addressList) {
        return addressList.get(0);
    }

    @Override
    public ApiResult<String> routeRun(TriggerParam triggerParam, List<String> addressList) {

        StringBuffer beatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ApiResult<String> beatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobDynamicScheduler.getExecutorBiz(address);
                beatResult = executorBiz.beat();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                beatResult = new ApiResult<String>(ApiResult.FAIL_CODE, "" + e);
            }
            beatResultSB.append((beatResultSB.length() > 0) ? "<br><br>" : "").append("心跳检测：").append("<br>address：")
                    .append(address).append("<br>code：").append(beatResult.getCode()).append("<br>msg：")
                    .append(beatResult.getMsg());

            // beat success
            if (beatResult.getCode() == ApiResult.SUCCESS_CODE) {

                ApiResult<String> runResult = XxlJobTrigger.runExecutor(triggerParam, address);
                beatResultSB.append("<br><br>").append(runResult.getMsg());

                // result
                runResult.setMsg(beatResultSB.toString());
                runResult.setContent(address);
                return runResult;
            }
        }
        return new ApiResult<String>(ApiResult.FAIL_CODE, beatResultSB.toString());

    }
}
