package com.xxl.job.spring;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Preconditions;

/**
 * @author haibo Date: 17-11-1 Time: 下午8:11
 */
public class TaskBean implements Serializable {

    private static final long serialVersionUID = -6040195763023843990L;

    private Object bean;

    private ConcurrentHashMap<String, Method> tasks = new ConcurrentHashMap<>();

    public Object getBean() {
        return bean;
    }

    public void setBean(Object bean) {
        this.bean = bean;
    }

    public void addTask(String name, Method method) {
        Preconditions.checkArgument(!tasks.contains(name), "不允许出现重名任务");
        tasks.put(name, method);
    }

    public void invoke(String name, TaskParam params) throws InvocationTargetException, IllegalAccessException {
        Method method = tasks.get(name);
        method.invoke(bean, params);
    }
}