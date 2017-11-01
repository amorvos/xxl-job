package com.xxl.job.admin.controller;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.xxl.job.admin.controller.annotation.PermessionLimit;
import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.NetComServerFactory;
import com.xxl.job.core.rpc.serialize.HessianSerializer;
import com.xxl.job.core.util.HttpClientUtil;

/**
 * Created by xuxueli on 17/5/10.
 */
@Controller
public class JobApiController {

    private static Logger LOGGER = LoggerFactory.getLogger(JobApiController.class);

    @RequestMapping("/api")
    @PermessionLimit(limit = false)
    public void api(HttpServletRequest request, HttpServletResponse response) throws IOException {

        RpcResponse rpcResponse = doInvoke(request);

        byte[] responseBytes = HessianSerializer.serialize(rpcResponse);
        response.setContentType("text/html;charset=utf-8");
        response.setStatus(HttpServletResponse.SC_OK);

        OutputStream out = response.getOutputStream();
        out.write(responseBytes);
        out.flush();
    }

    private RpcResponse doInvoke(HttpServletRequest request) {
        try {
            byte[] requestBytes = HttpClientUtil.readBytes(request);
            if (requestBytes == null || requestBytes.length == 0) {
                RpcResponse rpcResponse = new RpcResponse();
                rpcResponse.setError("RpcRequest byte[] is null");
                return rpcResponse;
            }
            RpcRequest rpcRequest = (RpcRequest) HessianSerializer.deserialize(requestBytes, RpcRequest.class);

            return NetComServerFactory.invokeService(rpcRequest);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            RpcResponse rpcResponse = new RpcResponse();
            rpcResponse.setError("Server-error:" + e.getMessage());
            return rpcResponse;
        }
    }
}
