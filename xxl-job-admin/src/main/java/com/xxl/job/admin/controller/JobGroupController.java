package com.xxl.job.admin.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Lists;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.dao.XxlJobGroupDao;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.valid.JobGroupAddressVerify;
import com.xxl.job.admin.valid.JobGroupVerify;
import com.xxl.job.api.model.ApiResult;

/**
 * job group controller
 * 
 * @author xuxueli 2016-10-02 20:52:56
 */
@Controller
@RequestMapping("/jobgroup")
public class JobGroupController {

    private static List<JobGroupVerify> verifies = Lists.newArrayList();

    @Resource
    private XxlJobGroupDao xxlJobGroupDao;

    @Resource
    private XxlJobInfoDao xxlJobInfoDao;

    static {
        verifies.add(new JobGroupAddressVerify());
    }

    @RequestMapping
    public String index(Model model) {
        model.addAttribute("list", xxlJobGroupDao.findAll());
        return "jobgroup/jobgroup.index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public ApiResult save(@Validated XxlJobGroup xxlJobGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResult.FAIL.setMsg(bindingResult.getFieldError().getDefaultMessage());
        }
        for (JobGroupVerify verify : verifies) {
            if (!verify.verify(xxlJobGroup)) {
                return ApiResult.FAIL.setMsg("机器地址非法");
            }
        }
        return (xxlJobGroupDao.save(xxlJobGroup) > 0) ? ApiResult.SUCCESS : ApiResult.FAIL;
    }

    @RequestMapping("/update")
    @ResponseBody
    public ApiResult update(@Validated XxlJobGroup xxlJobGroup, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ApiResult.FAIL.setMsg(bindingResult.getFieldError().getDefaultMessage());
        }
        for (JobGroupVerify verify : verifies) {
            if (!verify.verify(xxlJobGroup)) {
                return ApiResult.FAIL.setMsg("机器地址非法");
            }
        }
        int ret = xxlJobGroupDao.update(xxlJobGroup);
        return (ret > 0) ? ApiResult.SUCCESS : ApiResult.FAIL;
    }

    @RequestMapping("/remove")
    @ResponseBody
    public ApiResult remove(int id) {
        int count = xxlJobInfoDao.pageListCount(0, 10, id, null);
        if (count > 0) {
            return ApiResult.FAIL.setMsg("该分组使用中, 不可删除");
        }

        List<XxlJobGroup> allList = xxlJobGroupDao.findAll();
        if (allList.size() == 1) {
            return ApiResult.FAIL.setMsg("删除失败, 系统需要至少预留一个默认分组");
        }
        return (xxlJobGroupDao.remove(id) > 0) ? ApiResult.SUCCESS : ApiResult.FAIL;
    }

}
