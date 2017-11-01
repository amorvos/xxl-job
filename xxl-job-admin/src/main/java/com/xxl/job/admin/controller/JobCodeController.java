package com.xxl.job.admin.controller;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLogGlue;
import com.xxl.job.admin.dao.XxlJobInfoDao;
import com.xxl.job.admin.dao.XxlJobLogGlueDao;
import com.xxl.job.api.model.ApiResult;
import com.xxl.job.core.glue.GlueType;

/**
 * job code controller
 * 
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/jobcode")
public class JobCodeController {

    @Resource
    private XxlJobInfoDao xxlJobInfoDao;
    @Resource
    private XxlJobLogGlueDao xxlJobLogGlueDao;

    @RequestMapping
    public String index(Model model, int jobId) {
        XxlJobInfo jobInfo = xxlJobInfoDao.loadById(jobId);
        List<XxlJobLogGlue> jobLogGlues = xxlJobLogGlueDao.findByJobId(jobId);

        if (jobInfo == null) {
            throw new RuntimeException("抱歉，任务不存在.");
        }
        if (GlueType.BEAN == GlueType.match(jobInfo.getGlueType())) {
            throw new RuntimeException("该任务非GLUE模式.");
        }

        // Glue类型-字典
        model.addAttribute("GlueTypeEnum", GlueType.values());

        model.addAttribute("jobInfo", jobInfo);
        model.addAttribute("jobLogGlues", jobLogGlues);
        return "jobcode/jobcode.index";
    }

    @RequestMapping("/save")
    @ResponseBody
    public ApiResult save(Model model, int id, String glueSource, String glueRemark) {
        // valid
        if (glueRemark == null) {
            return ApiResult.FAIL.setMsg("请输入备注");
        }
        if (glueRemark.length() < 4 || glueRemark.length() > 100) {
            return ApiResult.FAIL.setMsg("备注长度应该在4至100之间");
        }
        XxlJobInfo jobInfo = xxlJobInfoDao.loadById(id);
        if (jobInfo == null) {
            return ApiResult.FAIL.setMsg("参数异常");
        }

        jobInfo.setGlueSource(glueSource);
        jobInfo.setGlueRemark(glueRemark);
        jobInfo.setGlueUpdatetime(new Date());
        xxlJobInfoDao.update(jobInfo);

        XxlJobLogGlue xxlJobLogGlue = new XxlJobLogGlue();
        xxlJobLogGlue.setJobId(jobInfo.getId());
        xxlJobLogGlue.setGlueType(jobInfo.getGlueType());
        xxlJobLogGlue.setGlueSource(glueSource);
        xxlJobLogGlue.setGlueRemark(glueRemark);
        xxlJobLogGlueDao.save(xxlJobLogGlue);
        xxlJobLogGlueDao.removeOld(jobInfo.getId(), 30);

        return ApiResult.SUCCESS;
    }

}
