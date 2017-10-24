package com.xxl.job.core.biz;

import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.api.handler.model.LogResult;
import com.xxl.job.api.handler.model.TriggerParam;

/**
 * Created by xuxueli on 17/3/1.
 */
public interface ExecutorBiz {

    /**
     * beat
     * 
     * @return
     */
    ApiResult<String> beat();

    /**
     * idle beat
     *
     * @param jobId
     * @return
     */
    ApiResult<String> idleBeat(int jobId);

    /**
     * kill
     * 
     * @param jobId
     * @return
     */
    ApiResult<String> kill(int jobId);

    /**
     * log
     * 
     * @param logDateTim
     * @param logId
     * @param fromLineNum
     * @return
     */
    ApiResult<LogResult> log(long logDateTim, int logId, int fromLineNum);

    /**
     * run
     * 
     * @param triggerParam
     * @return
     */
    ApiResult<String> run(TriggerParam triggerParam);

}
