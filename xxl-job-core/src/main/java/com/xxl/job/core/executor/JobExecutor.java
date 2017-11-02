package com.xxl.job.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import com.google.common.collect.Lists;
import com.xxl.job.api.annotation.Schedule;
import com.xxl.job.api.api.JobHandler;
import com.xxl.job.core.biz.AdminBiz;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.impl.DefaultExecutorBiz;
import com.xxl.job.core.log.XxlJobFileAppender;
import com.xxl.job.core.rpc.netcom.NetComClientProxy;
import com.xxl.job.core.rpc.netcom.NetComServerFactory;
import com.xxl.job.core.thread.JobThread;

/**
 * Created by xuxueli on 2016/3/2 21:14.
 */
public class JobExecutor {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private static ConcurrentHashMap<String, JobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Integer, JobThread> jobThreadRepository = new ConcurrentHashMap<>();

    private static List<AdminBiz> adminBizList = Lists.newArrayList();

    private static ApplicationContext applicationContext;

    private NetComServerFactory serverFactory = new NetComServerFactory();

    /**
     * 当前机器的Host地址
     */
    private String host;

    /**
     * 对外监听的端口号
     */
    private int port;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 配置中心地址
     */
    private String adminAddresses;

    /**
     * 应用的Token
     */
    private String accessToken;

    public void start() throws Exception {
        initAdminBizList(adminAddresses, accessToken);
        initExecutorServer(port, host, appName, accessToken);
    }

    private void initAdminBizList(String adminAddresses, String accessToken) throws Exception {
        if (adminAddresses == null || adminAddresses.trim().length() == 0) {
            return;
        }
        for (String address : adminAddresses.trim().split(",")) {
            if (address == null || address.trim().length() == 0) {
                continue;
            }
            String addressUrl = address.concat(AdminBiz.MAPPING);
            AdminBiz adminBiz = (AdminBiz) new NetComClientProxy(AdminBiz.class, addressUrl, accessToken).getObject();
            adminBizList.add(adminBiz);
        }
    }

    public void registJobHandler(Object bean) {
        if (!(bean instanceof JobHandler)) {
            return;
        }
        String name = bean.getClass().getAnnotation(Schedule.class).value();
        JobHandler handler = (JobHandler) bean;
        if (loadJobHandler(name) != null) {
            throw new RuntimeException("xxl-job jobhandler naming conflicts.");
        }
        registJobHandler(name, handler);
    }

    public static JobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }

    private void initExecutorServer(int port, String host, String appName, String accessToken) throws Exception {
        NetComServerFactory.putService(ExecutorBiz.class, new DefaultExecutorBiz());
        NetComServerFactory.setAccessToken(accessToken);
        serverFactory.start(port, host, appName);
    }

    private static JobHandler registJobHandler(String name, JobHandler jobHandler) {
        logger.info(">>>>>>>>>>> xxl-job register jobhandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    public void destroy() {
        if (jobThreadRepository.size() > 0) {
            for (Map.Entry<Integer, JobThread> item : jobThreadRepository.entrySet()) {
                removeJobThread(item.getKey(), "Web容器销毁终止");
            }
            jobThreadRepository.clear();
        }
        stopExecutorServer();
    }

    private void stopExecutorServer() {
        serverFactory.destroy();
    }

    public static JobThread registJobThread(int jobId, JobHandler handler, String removeOldReason) {
        JobThread newJobThread = new JobThread(jobId, handler);
        newJobThread.start();
        logger.info(">>>>>>>>>>> xxl-job regist JobThread success, jobId:{}, handler:{}", jobId, handler);

        JobThread oldJobThread = jobThreadRepository.put(jobId, newJobThread);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
    }

    public static void removeJobThread(int jobId, String removeOldReason) {
        JobThread oldJobThread = jobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
    }

    public void setLogPath(String logPath) {
        // init logpath
        if (StringUtils.isNotEmpty(logPath)) {
            XxlJobFileAppender.logPath = logPath;
        }
    }

    public static JobThread loadJobThread(int jobId) {
        return jobThreadRepository.get(jobId);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public void setAdminAddresses(String adminAddresses) {
        this.adminAddresses = adminAddresses;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        JobExecutor.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

}
