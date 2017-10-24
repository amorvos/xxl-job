package com.xxl.job.dao.impl;

import org.junit.Assert;
import org.junit.Test;

import com.xxl.job.api.handler.model.ApiResult;
import com.xxl.job.api.handler.model.RegistryParam;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.enums.RegistryConfig;
import com.xxl.job.core.rpc.netcom.NetComClientProxy;

/**
 * admin api test
 *
 * @author xuxueli 2017-07-28 22:14:52
 */
public class AdminBizTest {

    // admin-client
    private static String addressUrl = "http://127.0.0.1:8080/xxl-job-admin".concat(AdminBiz.MAPPING);
    private static String accessToken = null;

    /**
     * registry executor
     *
     * @throws Exception
     */
    @Test
    public void registryTest() throws Exception {
        AdminBiz adminBiz = (AdminBiz) new NetComClientProxy(AdminBiz.class, addressUrl, accessToken).getObject();

        // test executor registry
        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(),
                "xxl-job-executor-example", "127.0.0.1:9999");
        ApiResult<String> ApiResult = adminBiz.registry(registryParam);
        Assert.assertTrue(ApiResult.getCode() == ApiResult.SUCCESS_CODE);
    }

    /**
     * registry executor remove
     *
     * @throws Exception
     */
    @Test
    public void registryRemove() throws Exception {
        AdminBiz adminBiz = (AdminBiz) new NetComClientProxy(AdminBiz.class, addressUrl, accessToken).getObject();

        // test executor registry remove
        RegistryParam registryParam = new RegistryParam(RegistryConfig.RegistType.EXECUTOR.name(),
                "xxl-job-executor-example", "127.0.0.1:9999");
        ApiResult<String> ApiResult = adminBiz.registryRemove(registryParam);
        Assert.assertTrue(ApiResult.getCode() == ApiResult.SUCCESS_CODE);
    }

    /**
     * trigger job for once
     *
     * @throws Exception
     */
    @Test
    public void triggerJob() throws Exception {
        AdminBiz adminBiz = (AdminBiz) new NetComClientProxy(AdminBiz.class, addressUrl, accessToken).getObject();

        int jobId = 1;
        ApiResult<String> ApiResult = adminBiz.triggerJob(jobId);
        Assert.assertTrue(ApiResult.getCode() == ApiResult.SUCCESS_CODE);
    }

}
