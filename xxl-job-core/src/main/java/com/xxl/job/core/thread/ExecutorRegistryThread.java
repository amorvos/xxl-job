package com.xxl.job.core.thread;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.api.model.ApiResult;
import com.xxl.job.api.model.RegistryParam;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.constants.RegistryConstants;
import com.xxl.job.core.enums.RegistType;
import com.xxl.job.core.executor.JobExecutor;
import com.xxl.job.core.util.IpUtil;

/**
 * Created by xuxueli on 17/3/2.
 */
public class ExecutorRegistryThread extends Thread {

    private static Logger logger = LoggerFactory.getLogger(ExecutorRegistryThread.class);

    private static ExecutorRegistryThread instance = new ExecutorRegistryThread();

    public static ExecutorRegistryThread getInstance() {
        return instance;
    }

    private Thread registryThread;
    private volatile boolean toStop = false;

    public void start(final int port, final String ip, final String appName) {

        // valid
        if (appName == null || appName.trim().length() == 0) {
            logger.warn(">>>>>>>>>>>> xxl-job, executor registry config fail, appName is null.");
            return;
        }
        if (JobExecutor.getAdminBizList() == null) {
            logger.warn(">>>>>>>>>>>> xxl-job, executor registry config fail, adminAddresses is null.");
            return;
        }

        // executor address (generate addredd = ip:port)
        final String executorAddress;
        if (ip != null && ip.trim().length() > 0) {
            executorAddress = ip.trim().concat(":").concat(String.valueOf(port));
        } else {
            executorAddress = IpUtil.getIpPort(port);
        }

        registryThread = new Thread(new Runnable() {
            @Override
            public void run() {

                // registry
                while (!toStop) {
                    try {
                        RegistryParam registryParam = new RegistryParam(RegistType.EXECUTOR.name(), appName,
                                executorAddress);
                        for (AdminBiz adminBiz : JobExecutor.getAdminBizList()) {
                            try {
                                ApiResult registryResult = adminBiz.registry(registryParam);
                                if (registryResult != null && ApiResult.SUCCESS_CODE == registryResult.getCode()) {
                                    registryResult = ApiResult.SUCCESS;
                                    logger.info(
                                            ">>>>>>>>>>> xxl-job registry success, registryParam:{}, registryResult:{}",
                                            new Object[] { registryParam, registryResult });
                                    break;
                                } else {
                                    logger.info(
                                            ">>>>>>>>>>> xxl-job registry fail, registryParam:{}, registryResult:{}",
                                            new Object[] { registryParam, registryResult });
                                }
                            } catch (Exception e) {
                                logger.info(">>>>>>>>>>> xxl-job registry error, registryParam:{}", registryParam, e);
                            }

                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    try {
                        TimeUnit.SECONDS.sleep(RegistryConstants.BEAT_TIMEOUT);
                    } catch (InterruptedException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                // registry remove
                try {
                    RegistryParam registryParam = new RegistryParam(RegistType.EXECUTOR.name(), appName,
                            executorAddress);
                    for (AdminBiz adminBiz : JobExecutor.getAdminBizList()) {
                        try {
                            ApiResult registryResult = adminBiz.registryRemove(registryParam);
                            if (registryResult != null && ApiResult.SUCCESS_CODE == registryResult.getCode()) {
                                registryResult = ApiResult.SUCCESS;
                                logger.info(
                                        ">>>>>>>>>>> xxl-job registry-remove success, registryParam:{}, registryResult:{}",
                                        new Object[] { registryParam, registryResult });
                                break;
                            } else {
                                logger.info(
                                        ">>>>>>>>>>> xxl-job registry-remove fail, registryParam:{}, registryResult:{}",
                                        new Object[] { registryParam, registryResult });
                            }
                        } catch (Exception e) {
                            logger.info(">>>>>>>>>>> xxl-job registry-remove error, registryParam:{}", registryParam,
                                    e);
                        }

                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                logger.warn(">>>>>>>>>>>> xxl-job, executor registry thread destory.");

            }
        });
        registryThread.setDaemon(true);
        registryThread.start();
    }

    public void toStop() {
        toStop = true;
        // interrupt and wait
        registryThread.interrupt();
        try {
            registryThread.join();
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
