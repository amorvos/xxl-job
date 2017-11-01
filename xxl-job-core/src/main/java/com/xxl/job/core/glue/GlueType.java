package com.xxl.job.core.glue;

/**
 * Created by xuxueli on 17/4/26.
 */
public enum GlueType {

    BEAN("BEAN模式"),
    GLUE_GROOVY("GLUE模式(Java)"),
    GLUE_SHELL("GLUE模式(Shell)"),
    GLUE_PYTHON("GLUE模式(Python)"),
    GLUE_NODEJS("GLUE模式(Nodejs)");

    private String desc;
    private GlueType(String desc) {
        this.desc = desc;
    }
    public String getDesc() {
        return desc;
    }

    public static GlueType match(String name){
        for (GlueType item: GlueType.values()) {
            if (item.name().equals(name)) {
                return item;
            }
        }
        return null;
    }
}
