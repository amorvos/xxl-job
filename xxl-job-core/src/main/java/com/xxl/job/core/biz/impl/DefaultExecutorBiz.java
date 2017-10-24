package com.xxl.job.core.biz.impl;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.api.handler.api.JobHandler;
import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.api.handler.model.LogResult;
import com.xxl.job.api.handler.model.TriggerParam;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.executor.JobExecutor;
import com.xxl.job.core.glue.GlueFactory;
import com.xxl.job.core.glue.GlueTypeEnum;
import com.xxl.job.core.handler.GlueJobHandler;
import com.xxl.job.core.handler.ScriptJobHandler;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.thread.JobThread;

/**
 * Created by xuxueli on 17/3/1.
 */
public class DefaultExecutorBiz implements ExecutorBiz {

    private static Logger logger = LoggerFactory.getLogger(DefaultExecutorBiz.class);

    @Override
    public ApiResult<String> beat() {
        return ApiResult.SUCCESS;
    }

    @Override
    public ApiResult<String> idleBeat(int jobId) {

        // isRunningOrHasQueue
        boolean isRunningOrHasQueue = false;
        JobThread jobThread = JobExecutor.loadJobThread(jobId);
        if (jobThread != null && jobThread.isRunningOrHasQueue()) {
            isRunningOrHasQueue = true;
        }

        if (isRunningOrHasQueue) {
            return new ApiResult<String>(ApiResult.FAIL_CODE, "job thread is running or has trigger queue.");
        }
        return ApiResult.SUCCESS;
    }

    @Override
    public ApiResult<String> kill(int jobId) {
        // kill handlerThread, and create new one
        JobThread jobThread = JobExecutor.loadJobThread(jobId);
        if (jobThread != null) {
            JobExecutor.removeJobThread(jobId, "人工手动终止");
            return ApiResult.SUCCESS;
        }

        return new ApiResult<String>(ApiResult.SUCCESS_CODE, "job thread aleady killed.");
    }

    @Override
    public ApiResult<LogResult> log(long logDateTim, int logId, int fromLineNum) {
        // log filename: yyyy-MM-dd/9999.log
        String logFileName = XxlJobFileAppender.makeLogFileName(new Date(logDateTim), logId);

        LogResult logResult = XxlJobFileAppender.readLog(logFileName, fromLineNum);
        return new ApiResult<LogResult>(logResult);
    }

    @Override
    public ApiResult<String> run(TriggerParam triggerParam) {
        // load old：jobHandler + jobThread
        JobThread jobThread = JobExecutor.loadJobThread(triggerParam.getJobId());
        JobHandler jobHandler = jobThread != null ? jobThread.getHandler() : null;
        String removeOldReason = null;

        // valid：jobHandler + jobThread
        if (GlueTypeEnum.BEAN == GlueTypeEnum.match(triggerParam.getGlueType())) {

            // new jobhandler
            JobHandler newJobHandler = JobExecutor.loadJobHandler(triggerParam.getExecutorHandler());

            // valid old jobThread
            if (jobThread != null && jobHandler != newJobHandler) {
                // change handler, need kill old thread
                removeOldReason = "更换JobHandler或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                jobHandler = newJobHandler;
                if (jobHandler == null) {
                    return new ApiResult<String>(ApiResult.FAIL_CODE,
                            "job handler [" + triggerParam.getExecutorHandler() + "] not found.");
                }
            }

        } else if (GlueTypeEnum.GLUE_GROOVY == GlueTypeEnum.match(triggerParam.getGlueType())) {

            // valid old jobThread
            if (jobThread != null
                    && !(jobThread.getHandler() instanceof GlueJobHandler && ((GlueJobHandler) jobThread.getHandler())
                            .getGlueUpdatetime() == triggerParam.getGlueUpdatetime())) {
                // change handler or gluesource updated, need kill old thread
                removeOldReason = "更新任务逻辑或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                try {
                    JobHandler originJobHandler = GlueFactory.getInstance()
                            .loadNewInstance(triggerParam.getGlueSource());
                    jobHandler = new GlueJobHandler(originJobHandler, triggerParam.getGlueUpdatetime());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    return new ApiResult<String>(ApiResult.FAIL_CODE, e.getMessage());
                }
            }
        } else if (GlueTypeEnum.GLUE_SHELL == GlueTypeEnum.match(triggerParam.getGlueType())
                || GlueTypeEnum.GLUE_PYTHON == GlueTypeEnum.match(triggerParam.getGlueType())
                || GlueTypeEnum.GLUE_NODEJS == GlueTypeEnum.match(triggerParam.getGlueType())) {

            // valid old jobThread
            if (jobThread != null && !(jobThread.getHandler() instanceof ScriptJobHandler
                    && ((ScriptJobHandler) jobThread.getHandler()).getGlueUpdatetime() == triggerParam
                            .getGlueUpdatetime())) {
                // change script or gluesource updated, need kill old thread
                removeOldReason = "更新任务逻辑或更换任务模式,终止旧任务线程";

                jobThread = null;
                jobHandler = null;
            }

            // valid handler
            if (jobHandler == null) {
                jobHandler = new ScriptJobHandler(triggerParam.getJobId(), triggerParam.getGlueUpdatetime(),
                        triggerParam.getGlueSource(), GlueTypeEnum.match(triggerParam.getGlueType()));
            }
        } else {
            return new ApiResult<String>(ApiResult.FAIL_CODE,
                    "glueType[" + triggerParam.getGlueType() + "] is not valid.");
        }

        // executor block strategy
        if (jobThread != null) {
            ExecutorBlockStrategyEnum blockStrategy = ExecutorBlockStrategyEnum
                    .match(triggerParam.getExecutorBlockStrategy(), null);
            if (ExecutorBlockStrategyEnum.DISCARD_LATER == blockStrategy) {
                // discard when running
                if (jobThread.isRunningOrHasQueue()) {
                    return new ApiResult<String>(ApiResult.FAIL_CODE,
                            "阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.DISCARD_LATER.getTitle());
                }
            } else if (ExecutorBlockStrategyEnum.COVER_EARLY == blockStrategy) {
                // kill running jobThread
                if (jobThread.isRunningOrHasQueue()) {
                    removeOldReason = "阻塞处理策略-生效：" + ExecutorBlockStrategyEnum.COVER_EARLY.getTitle();

                    jobThread = null;
                }
            } else {
                // just queue trigger
            }
        }

        // replace thread (new or exists invalid)
        if (jobThread == null) {
            jobThread = JobExecutor.registJobThread(triggerParam.getJobId(), jobHandler, removeOldReason);
        }

        // push data to queue
        ApiResult<String> pushResult = jobThread.pushTriggerQueue(triggerParam);
        return pushResult;
    }

}
