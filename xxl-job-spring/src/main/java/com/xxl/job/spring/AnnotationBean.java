package com.xxl.job.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.annotation.AnnotationUtils;

import com.xxl.job.api.annotation.Schedule;
import com.xxl.job.core.executor.JobExecutor;

public class AnnotationBean implements BeanPostProcessor, ApplicationContextAware, ApplicationListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AnnotationBean.class);

    private JobExecutor jobExecutor;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        jobExecutor = BeanFactoryUtils.beanOfType(applicationContext, JobExecutor.class);
        jobExecutor.setApplicationContext(applicationContext);
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Schedule annotation = AnnotationUtils.findAnnotation(bean.getClass(), Schedule.class);
        if (annotation != null) {
            jobExecutor.registJobHandler(bean);
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ApplicationEvent event) {
        try {
            jobExecutor.start();
        } catch (Exception e) {
            LOGGER.error("JobExecutor START ERROR", e);
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
