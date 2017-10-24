package com.xxl.job.admin.core.route.strategy;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.schedule.XxlJobDynamicScheduler;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.api.handler.model.TriggerParam;
import com.xxl.job.core.biz.ExecutorBiz;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteBusyover extends ExecutorRouter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExecutorRouteBusyover.class);

    public String route(int jobId, ArrayList<String> addressList) {
        return addressList.get(0);
    }

    @Override
    public ApiResult<String> routeRun(TriggerParam triggerParam, List<String> addressList) {

        StringBuffer idleBeatResultSB = new StringBuffer();
        for (String address : addressList) {
            // beat
            ApiResult<String> idleBeatResult = null;
            try {
                ExecutorBiz executorBiz = XxlJobDynamicScheduler.getExecutorBiz(address);
                idleBeatResult = executorBiz.idleBeat(triggerParam.getJobId());
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
                idleBeatResult = new ApiResult<String>(ApiResult.FAIL_CODE, "" + e);
            }
            idleBeatResultSB.append((idleBeatResultSB.length() > 0) ? "<br><br>" : "").append("空闲检测：")
                    .append("<br>address：").append(address).append("<br>code：").append(idleBeatResult.getCode())
                    .append("<br>msg：").append(idleBeatResult.getMsg());

            // beat success
            if (idleBeatResult.getCode() == ApiResult.SUCCESS_CODE) {

                ApiResult<String> runResult = XxlJobTrigger.runExecutor(triggerParam, address);
                idleBeatResultSB.append("<br><br>").append(runResult.getMsg());

                // result
                runResult.setMsg(idleBeatResultSB.toString());
                runResult.setContent(address);
                return runResult;
            }
        }

        return new ApiResult<String>(ApiResult.FAIL_CODE, idleBeatResultSB.toString());
    }
}
