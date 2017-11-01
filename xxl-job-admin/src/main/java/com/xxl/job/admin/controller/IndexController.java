package com.xxl.job.admin.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.xxl.job.admin.controller.annotation.PermessionLimit;
import com.xxl.job.admin.controller.interceptor.PermissionInterceptor;
import com.xxl.job.admin.core.util.PropertiesUtil;
import com.xxl.job.admin.service.XxlJobService;
import com.xxl.job.api.model.ApiResult;

/**
 * index controller
 * 
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
public class IndexController {

    @Resource
    private XxlJobService xxlJobService;

    @RequestMapping("/")
    public String index(Model model) {
        Map<String, Object> dashboardMap = xxlJobService.dashboardInfo();
        model.addAllAttributes(dashboardMap);
        return "index";
    }

    @RequestMapping("/triggerChartDate")
    @ResponseBody
    public ApiResult triggerChartDate() {
        return ApiResult.SUCCESS.setContent(xxlJobService.triggerChartDate());
    }

    @RequestMapping("/toLogin")
    @PermessionLimit(limit = false)
    public String toLogin(HttpServletRequest request) {
        return PermissionInterceptor.ifLogin(request) ? "redirect:/" : "login";
    }

    @RequestMapping(value = "login", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public ApiResult loginDo(HttpServletRequest request, HttpServletResponse response, String userName, String password,
            String ifRemember) {
        if (!PermissionInterceptor.ifLogin(request)) {
            if (StringUtils.isNotBlank(userName) && StringUtils.isNotBlank(password)
                    && PropertiesUtil.getString("xxl.job.login.username").equals(userName)
                    && PropertiesUtil.getString("xxl.job.login.password").equals(password)) {
                boolean ifRem = false;
                if (StringUtils.isNotBlank(ifRemember) && "on".equals(ifRemember)) {
                    ifRem = true;
                }
                PermissionInterceptor.login(response, ifRem);
            } else {
                return ApiResult.FAIL.setMsg("账号或密码错误");
            }
        }
        return ApiResult.SUCCESS;
    }

    @RequestMapping(value = "logout", method = RequestMethod.POST)
    @ResponseBody
    @PermessionLimit(limit = false)
    public ApiResult logout(HttpServletRequest request, HttpServletResponse response) {
        if (PermissionInterceptor.ifLogin(request)) {
            PermissionInterceptor.logout(request, response);
        }
        return ApiResult.SUCCESS;
    }

    @RequestMapping("/help")
    public String help() {
        return "help";
    }

}
