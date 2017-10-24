package com.xxl.job.admin.service;

import java.util.Map;

import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.api.handler.model.ApiResult;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

    public Map<String, Object> pageList(int start, int length, int jobGroup, String executorHandler, String filterTime);

    ApiResult<String> add(XxlJobInfo jobInfo);

    ApiResult<String> reschedule(XxlJobInfo jobInfo);

    ApiResult<String> remove(int id);

    ApiResult<String> pause(int id);

    ApiResult<String> resume(int id);

    ApiResult<String> triggerJob(int id);

    Map<String, Object> dashboardInfo();

    ApiResult<Map<String, Object>> triggerChartDate();

}
