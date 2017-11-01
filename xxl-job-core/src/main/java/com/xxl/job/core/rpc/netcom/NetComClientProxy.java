package com.xxl.job.core.rpc.netcom;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.FactoryBean;

import com.xxl.job.core.rpc.codec.RpcRequest;
import com.xxl.job.core.rpc.codec.RpcResponse;
import com.xxl.job.core.rpc.netcom.jetty.client.JettyClient;

/**
 * rpc proxy
 * 
 * @author xuxueli 2015-10-29 20:18:32
 */
public class NetComClientProxy implements FactoryBean<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(NetComClientProxy.class);

    private JettyClient client = new JettyClient();

    private String serverAddress;

    private String accessToken;

    private Class<?> clazz;

    public NetComClientProxy(Class<?> clazz, String serverAddress, String accessToken) {
        this.clazz = clazz;
        this.serverAddress = serverAddress;
        this.accessToken = accessToken;
    }

    @Override
    public Object getObject() throws Exception {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { clazz },
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                        // filter method like "Object.toString()"
                        if (Object.class.getName().equals(method.getDeclaringClass().getName())) {
                            LOGGER.error(">>>>>>>>>>> xxl-rpc proxy class-method not support [{}.{}]",
                                    method.getDeclaringClass().getName(), method.getName());
                            throw new RuntimeException("xxl-rpc proxy class-method not support");
                        }

                        // request
                        RpcRequest request = new RpcRequest();
                        request.setServerAddress(serverAddress);
                        request.setCreateMillisTime(System.currentTimeMillis());
                        request.setAccessToken(accessToken);
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        // send
                        RpcResponse response = client.send(request);

                        // valid response
                        if (response == null) {
                            LOGGER.error(">>>>>>>>>>> xxl-rpc netty response not found.");
                            throw new Exception(">>>>>>>>>>> xxl-rpc netty response not found.");
                        }
                        if (response.isError()) {
                            throw new RuntimeException(response.getError());
                        } else {
                            return response.getResult();
                        }
                    }
                });
    }

    @Override
    public Class<?> getObjectType() {
        return clazz;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

}
