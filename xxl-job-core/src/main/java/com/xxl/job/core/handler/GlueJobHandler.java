package com.xxl.job.core.handler;

import com.xxl.job.api.handler.api.JobHandler;
import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.core.log.XxlJobLogger;

/**
 * glue job handler
 * 
 * @author xuxueli 2016-5-19 21:05:45
 */
public class GlueJobHandler extends JobHandler {

    private long glueUpdatetime;
    private JobHandler jobHandler;

    public GlueJobHandler(JobHandler jobHandler, long glueUpdatetime) {
        this.jobHandler = jobHandler;
        this.glueUpdatetime = glueUpdatetime;
    }

    public long getGlueUpdatetime() {
        return glueUpdatetime;
    }

    @Override
    public ApiResult<String> execute(String... params) throws Exception {
        XxlJobLogger.log("----------- glue.version:" + glueUpdatetime + " -----------");
        return jobHandler.execute(params);
    }

}
