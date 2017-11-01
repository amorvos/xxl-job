package com.xxl.job.admin.valid;

import org.apache.commons.lang.StringUtils;

import com.xxl.job.admin.core.model.XxlJobGroup;

/**
 * @author haibo Date: 17-10-26 Time: 下午4:28
 */
public class JobGroupAddressVerify implements JobGroupVerify {

    @Override
    public boolean verify(XxlJobGroup jobGroup) {
        if (jobGroup == null) {
            return true;
        }
        if (jobGroup.getAddressType() != 0) {
            if (StringUtils.isBlank(jobGroup.getAddressList())) {
                return false;
            }
            String[] addresss = jobGroup.getAddressList().split(",");
            for (String item : addresss) {
                if (StringUtils.isBlank(item)) {
                    return false;
                }
            }
        }
        return true;
    }

}