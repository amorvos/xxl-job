package com.xxl.job.admin.service;

import java.util.Map;

import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.api.model.ApiResult;

/**
 * core job action for xxl-job
 * 
 * @author xuxueli 2016-5-28 15:30:33
 */
public interface XxlJobService {

    Map<String, Object> pageList(int start, int length, int jobGroup, String executorHandler, String filterTime);

    ApiResult<String> add(XxlJobInfo jobInfo);

    ApiResult<String> reschedule(XxlJobInfo jobInfo);

    ApiResult<String> remove(int id);

    ApiResult<String> pause(int id);

    ApiResult<String> resume(int id);

    ApiResult<String> triggerJob(int id);

    Map<String, Object> dashboardInfo();

    Map<String, Object> triggerChartDate();

}
