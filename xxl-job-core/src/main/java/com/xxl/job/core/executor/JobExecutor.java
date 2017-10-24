package com.xxl.job.core.executor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.collect.Lists;
import com.xxl.job.api.handler.annotation.Schedule;
import com.xxl.job.api.handler.api.JobHandler;
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
public class JobExecutor implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(JobExecutor.class);

    private static ConcurrentHashMap<String, JobHandler> jobHandlerRepository = new ConcurrentHashMap<>();

    private static ConcurrentHashMap<Integer, JobThread> JobThreadRepository = new ConcurrentHashMap<>();

    private static List<AdminBiz> adminBizList = Lists.newArrayList();

    private static ApplicationContext applicationContext;

    /**
     * 当前机器的Host地址
     */
    private String host;

    /**
     * 对外监听的端口号，默认是9999
     */
    private int port = 9999;

    /**
     * 应用名
     */
    private String appName;

    /**
     * 配置中心地址
     */
    // TODO 这里现在是直接配置死的，可以改成ZK模式
    private String adminAddresses;

    /**
     * 应用的Token
     */
    // TODO 现在的实现是可以没有这个Token，存在认证的情况下才可以向注册中心进行服务的自发现
    private String accessToken;

    /**
     * 调度日志存放位置
     */
    private String logPath;

    private NetComServerFactory serverFactory = new NetComServerFactory();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        JobExecutor.applicationContext = applicationContext;
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void start() throws Exception {
        // init admin-client
        initAdminBizList(adminAddresses, accessToken);

        // init executor-jobHandlerRepository
        if (applicationContext != null) {
            initJobHandlerRepository(applicationContext);
        }

        // init logpath
        if (logPath != null && logPath.trim().length() > 0) {
            XxlJobFileAppender.logPath = logPath;
        }

        // init executor-server
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

    private void initJobHandlerRepository(ApplicationContext applicationContext) {
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(Schedule.class);
        if (serviceBeanMap == null || serviceBeanMap.size() == 0) {
            return;
        }
        for (Object serviceBean : serviceBeanMap.values()) {
            if (!(serviceBean instanceof JobHandler)) {
                continue;
            }
            String name = serviceBean.getClass().getAnnotation(Schedule.class).value();
            JobHandler handler = (JobHandler) serviceBean;
            if (loadJobHandler(name) != null) {
                throw new RuntimeException("xxl-job jobhandler naming conflicts.");
            }
            registJobHandler(name, handler);
        }
    }

    private void initExecutorServer(int port, String ip, String appName, String accessToken) throws Exception {
        NetComServerFactory.putService(ExecutorBiz.class, new DefaultExecutorBiz());
        NetComServerFactory.setAccessToken(accessToken);
        serverFactory.start(port, ip, appName);
    }

    public static JobHandler loadJobHandler(String name) {
        return jobHandlerRepository.get(name);
    }

    public static JobHandler registJobHandler(String name, JobHandler jobHandler) {
        logger.info(">>>>>>>>>>> xxl-job register jobhandler success, name:{}, jobHandler:{}", name, jobHandler);
        return jobHandlerRepository.put(name, jobHandler);
    }

    public void destroy() {
        if (JobThreadRepository.size() > 0) {
            for (Map.Entry<Integer, JobThread> item : JobThreadRepository.entrySet()) {
                removeJobThread(item.getKey(), "Web容器销毁终止");
            }
            JobThreadRepository.clear();
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

        JobThread oldJobThread = JobThreadRepository.put(jobId, newJobThread);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }

        return newJobThread;
    }

    public static void removeJobThread(int jobId, String removeOldReason) {
        JobThread oldJobThread = JobThreadRepository.remove(jobId);
        if (oldJobThread != null) {
            oldJobThread.toStop(removeOldReason);
            oldJobThread.interrupt();
        }
    }

    public static JobThread loadJobThread(int jobId) {
        return JobThreadRepository.get(jobId);
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

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public static List<AdminBiz> getAdminBizList() {
        return adminBizList;
    }

}
