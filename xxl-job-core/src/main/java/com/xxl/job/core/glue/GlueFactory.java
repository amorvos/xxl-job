package com.xxl.job.core.glue;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.annotation.AnnotationUtils;

import com.xxl.job.api.api.JobHandler;
import com.xxl.job.core.executor.JobExecutor;

import groovy.lang.GroovyClassLoader;

/**
 * glue factory, product class/object by name
 * 
 * @author xuxueli 2016-1-2 20:02:27
 */
public class GlueFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlueFactory.class);

    private static GlueFactory glueFactory = new GlueFactory();

    /**
     * groovy class loader
     */
    private GroovyClassLoader groovyClassLoader = new GroovyClassLoader();

    public static GlueFactory getInstance() {
        return glueFactory;
    }

    // ----------------------------- load instance -----------------------------
    // load new instance, prototype
    public JobHandler loadNewInstance(String codeSource) throws Exception {
        if (codeSource != null && codeSource.trim().length() > 0) {
            Class<?> clazz = groovyClassLoader.parseClass(codeSource);
            if (clazz != null) {
                Object instance = clazz.newInstance();
                if (instance != null) {
                    if (instance instanceof JobHandler) {
                        injectService(instance);
                        return (JobHandler) instance;
                    } else {
                        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, "
                                + "cannot convert from instance[" + instance.getClass() + "] to JobHandler");
                    }
                }
            }
        }
        throw new IllegalArgumentException(">>>>>>>>>>> xxl-glue, loadNewInstance error, instance is null");
    }

    private void injectService(Object instance) {
        if (instance == null) {
            return;
        }
        Field[] fields = instance.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object fieldBean = null;
            if (AnnotationUtils.getAnnotation(field, Resource.class) != null) {
                fieldBean = fetchResource(field);
                if (fieldBean == null) {
                    fieldBean = JobExecutor.getApplicationContext().getBean(field.getType());
                }
            } else if (AnnotationUtils.getAnnotation(field, Autowired.class) != null) {
                Qualifier qualifier = AnnotationUtils.getAnnotation(field, Qualifier.class);
                if (qualifier != null && qualifier.value().length() > 0) {
                    fieldBean = JobExecutor.getApplicationContext().getBean(qualifier.value());
                } else {
                    fieldBean = JobExecutor.getApplicationContext().getBean(field.getType());
                }
            }
            if (fieldBean != null) {
                field.setAccessible(true);
                try {
                    field.set(instance, fieldBean);
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    LOGGER.error(" field.set(instance, fieldBean) error ", e);
                }
            }
        }
    }

    private Object fetchResource(Field field) {
        try {
            Resource resource = AnnotationUtils.getAnnotation(field, Resource.class);
            if (resource != null && resource.name().length() > 0) {
                return JobExecutor.getApplicationContext().getBean(resource.name());
            } else {
                return JobExecutor.getApplicationContext().getBean(field.getName());
            }
        } catch (Exception e) {
            LOGGER.error("ADD fillBean RESOURCE ERROR", e);
        }
        return null;
    }

}
