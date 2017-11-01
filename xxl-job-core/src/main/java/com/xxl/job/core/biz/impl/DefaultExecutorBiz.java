package com.xxl.job.core.biz.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.api.api.JobHandler;
import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.TriggerParam;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.enums.ExecutorBlockType;
import com.xxl.job.core.executor.JobExecutor;
import com.xxl.job.core.glue.GlueType;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobThread;

/**
 * Created by xuxueli on 17/3/1.
 */
@SuppressWarnings("unchecked")
public class DefaultExecutorBiz implements ExecutorBiz {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultExecutorBiz.class);

    @Override
    public ApiResult beat() {
        return ApiResult.SUCCESS;
    }

    @Override
    public ApiResult idleBeat(int jobId) {

        // isRunningOrHasQueue
        boolean isRunningOrHasQueue = false;
        JobThread jobThread = JobExecutor.loadJobThread(jobId);
        if (jobThread != null && jobThread.isRunningOrHasQueue()) {
            isRunningOrHasQueue = true;
        }

        if (isRunningOrHasQueue) {
            return ApiResult.FAIL.setMsg("job thread is running or has trigger queue.");
        }
        return ApiResult.SUCCESS;
    }

    @Override
    public boolean kill(int jobId) {
        try {
            JobThread jobThread = JobExecutor.loadJobThread(jobId);
            if (jobThread != null) {
                JobExecutor.removeJobThread(jobId, "人工手动终止");
            }
            return true;
        } catch (Exception e) {
            LOGGER.error("kill job error， jobId：{}", jobId, e);
            return false;
        }
    }

    @Override
    public ApiResult log(long logDateTim, int logId, int fromLineNum) {
        // log filename: yyyy-MM-dd/9999.log
        String logFileName = XxlJobFileAppender.makeLogFileName(new Date(logDateTim), logId);
        return ApiResult.SUCCESS.setContent(XxlJobFileAppender.readLog(logFileName, fromLineNum));
    }

    @Override
    public ApiResult run(TriggerParam triggerParam) {
        JobThread jobThread = JobExecutor.loadJobThread(triggerParam.getJobId());
        JobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;

        GlueType glueType = GlueType.match(triggerParam.getGlueType());
        if (GlueType.BEAN != glueType) {
            return ApiResult.FAIL.setMsg("glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }

        JobHandler nowJobHandler = JobExecutor.loadJobHandler(triggerParam.getExecutorHandler());
        if (jobThread != null && jobHandler != nowJobHandler) {
            // change handler, need kill old thread
            removeOldReason = "更换JobHandler或更换任务模式,终止旧任务线程";
            LOGGER.warn(removeOldReason);
            jobThread = null;
        }

        if (jobHandler == null) {
            jobHandler = nowJobHandler;
            if (jobHandler == null) {
                return ApiResult.FAIL.setMsg("job handler [" + triggerParam.getExecutorHandler() + "] not found.");
            }
        }

        if (jobThread != null) {
            ExecutorBlockType blockType = ExecutorBlockType.match(triggerParam.getExecutorBlockStrategy(), null);
            if (ExecutorBlockType.DISCARD_LATER == blockType) {
                // discard when running
                if (jobThread.isRunningOrHasQueue()) {
                    return ApiResult.FAIL.setMsg("阻塞处理策略-生效：" + ExecutorBlockType.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockType.COVER_EARLY == blockType && jobThread.isRunningOrHasQueue()) {
                // kill running jobThread
                removeOldReason = "阻塞处理策略-生效：" + ExecutorBlockType.COVER_EARLY.getTitle();
                LOGGER.warn(removeOldReason);
                jobThread = null;
            }
        }

        // replace thread (new or exists invalid)
        if (jobThread == null) {
            jobThread = JobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
        }

        // push data to queue
        return jobThread.pushTriggerQueue(triggerParam);
    }

}
