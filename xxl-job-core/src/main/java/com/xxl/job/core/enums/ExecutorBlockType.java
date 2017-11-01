package com.xxl.job.core.enums;

/**
 * Created by xuxueli on 17/5/9.
 */
public enum ExecutorBlockType {

    SERIAL_EXECUTION("单机串行"), DISCARD_LATER("丢弃后续调度"), COVER_EARLY("覆盖之前调度");

    private final String title;

    private ExecutorBlockType(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public static ExecutorBlockType match(String name, ExecutorBlockType defaultItem) {
        if (name != null) {
            for (ExecutorBlockType item : ExecutorBlockType.values()) {
                if (item.name().equals(name)) {
                    return item;
                }
            }
        }
        return defaultItem;
    }
}
