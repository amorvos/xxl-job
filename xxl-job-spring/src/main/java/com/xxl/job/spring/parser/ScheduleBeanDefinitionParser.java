package com.xxl.job.spring.parser;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.google.common.base.Strings;
import com.xxl.job.core.executor.JobExecutor;
import com.xxl.job.spring.AnnotationBean;

/**
 * @author haibo Date: 17-11-1 Time: 下午6:00
 */
public class ScheduleBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    private static final String QSCHEDULE_ID = "QUNAR_QSCHEDULE_ID";

    private static final String QSCHEDULE_ANNOTATION = "QSCHEDULE_ANNOTATION";

    private volatile boolean init = false;

    @Override
    protected Class<?> getBeanClass(Element element) {
        return JobExecutor.class;
    }

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        Preconditions.checkArgument(!init, "一个应用中只允许配置一个<schedule:config>");

        String host = element.getAttribute("host");
        Preconditions.checkArgument(StringUtils.isNotEmpty(host));
        builder.addPropertyValue("host", host);

        String port = element.getAttribute("port");
        Preconditions.checkArgument(StringUtils.isNotEmpty(port));
        builder.addPropertyValue("port", port);

        String appName = element.getAttribute("appName");
        Preconditions.checkArgument(StringUtils.isNotEmpty(appName));
        builder.addPropertyValue("appName", appName);

        String adminAddresses = element.getAttribute("adminAddresses");
        Preconditions.checkArgument(StringUtils.isNotEmpty(adminAddresses));
        builder.addPropertyValue("adminAddresses", adminAddresses);

        String logPath = element.getAttribute("logPath");
        if (!Strings.isNullOrEmpty(logPath)) {
            builder.addPropertyValue("logPath", logPath);
        }
        String accessToken = element.getAttribute("accessToken");
        if (StringUtils.isNotEmpty(accessToken)) {
            builder.addPropertyValue("accessToken", accessToken);
        }
        if (!parserContext.getRegistry().containsBeanDefinition(QSCHEDULE_ANNOTATION)) {
            RootBeanDefinition annotation = new RootBeanDefinition(AnnotationBean.class);
            parserContext.getRegistry().registerBeanDefinition(QSCHEDULE_ANNOTATION, annotation);
        }
        init = true;
    }

    @Override
    protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) {
        return QSCHEDULE_ID;
    }
}