package com.xxl.job.admin.core.route.strategy;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import com.xxl.job.admin.core.route.ExecutorRouter;
import com.xxl.job.admin.core.trigger.XxlJobTrigger;
import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/10.
 */
public class ExecutorRouteRound extends ExecutorRouter {

    private static ConcurrentHashMap<Integer, Integer> routeCountEachJob = new ConcurrentHashMap<Integer, Integer>();
    private static long CACHE_VALID_TIME = 0;

    private static int count(int jobId) {
        // cache clear
        if (System.currentTimeMillis() > CACHE_VALID_TIME) {
            routeCountEachJob.clear();
            CACHE_VALID_TIME = System.currentTimeMillis() + 1000 * 60 * 60 * 24;
        }

        // count++
        Integer count = routeCountEachJob.get(jobId);
        count = (count == null || count > 1000000) ? (new Random().nextInt(100)) : ++count; // 初始化时主动Random一次，缓解首次压力
        routeCountEachJob.put(jobId, count);
        return count;
    }

    public String route(int jobId, List<String> addressList) {
        return addressList.get(count(jobId) % addressList.size());
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
