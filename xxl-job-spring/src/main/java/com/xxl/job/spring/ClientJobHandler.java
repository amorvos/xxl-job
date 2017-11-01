package com.xxl.job.spring;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.api.model.ApiResult;

/**
 * @author haibo Date: 17-11-1 Time: 下午8:10
 */
public class ClientJobHandler {

    public static final Logger LOGGER = LoggerFactory.getLogger(ClientJobHandler.class);

    private ConcurrentHashMap<String, TaskBean> taskBeans = new ConcurrentHashMap<>();

    public ApiResult execute(String taskName, String taskParam) throws Exception {
        TaskBean taskBean = taskBeans.get(taskName);
        TaskParam params = buildParams(taskBean);
        taskBean.invoke(taskName, params);
        return null;
    }

    private TaskParam buildParams(TaskBean taskBean) {
        return null;
    }
}