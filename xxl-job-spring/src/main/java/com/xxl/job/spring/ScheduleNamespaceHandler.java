package com.xxl.job.spring;

import com.xxl.job.spring.parser.ScheduleBeanDefinitionParser;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class ScheduleNamespaceHandler extends NamespaceHandlerSupport {

    @Override
    public void init() {
        registerBeanDefinitionParser("config", new ScheduleBeanDefinitionParser());
    }

}
