package com.xxl.job.executor.service.jobhandler;

import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Component;

import com.xxl.job.api.handler.annotation.Schedule;
import com.xxl.job.api.handler.api.JobHandler;
import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.core.log.XxlJobLogger;

/**
 * 任务Handler的一个Demo（Bean模式）
 * 
 * 开发步骤： 1、新建一个继承com.xxl.job.core.handler.IJobHandler的Java类； 2、该类被Spring容器扫描为Bean实例，如加“@Component”注解； 3、添加
 * “@JobHander(value="自定义jobhandler名称")”注解，注解的value值为自定义的JobHandler名称，该名称对应的是调度中心新建任务的JobHandler属性的值。 4、执行日志：需要通过
 * "XxlJobLogger.log" 打印执行日志；
 * 
 * @author xuxueli 2015-12-19 19:43:36
 */
@Schedule(value = "demoJobHandler")
@Component
public class DemoJobHandler extends JobHandler {

    @Override
    public ApiResult<String> execute(String... params) throws Exception {
        XxlJobLogger.log("XXL-JOB, Hello World.");

        for (int i = 0; i < 5; i++) {
            XxlJobLogger.log("beat at:" + i);
            TimeUnit.SECONDS.sleep(2);
        }
        return ApiResult.SUCCESS;
    }

}
