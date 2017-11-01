package com.xxl.job.core.rpc.netcom.jetty.server;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.thread.ExecutorThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.xxl.job.core.thread.ExecutorRegistryThread;
import com.xxl.job.core.thread.TriggerCallbackThread;

/**
 * rpc jetty server
 * 
 * @author xuxueli 2015-11-19 22:29:03
 */
@SuppressWarnings("unchecked")
public class JettyServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JettyServer.class);

    private Server server;

    private Thread thread;

    public void start(final int port, final String host, final String appName) {
        thread = new Thread(new ConnectRunner(appName, host, port));
        thread.setDaemon(true);
        thread.start();
    }

    public void destroy() {
        // destroy Registry-Server
        ExecutorRegistryThread.getInstance().toStop();

        // destroy Callback-Server
        TriggerCallbackThread.getInstance().toStop();

        // destroy server
        if (server != null) {
            try {
                server.stop();
                server.destroy();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        if (thread.isAlive()) {
            thread.interrupt();
        }
        LOGGER.info(">>>>>>>>>>> xxl-rpc server destroy success, netcon={}", JettyServer.class.getName());
    }

    class ConnectRunner implements Runnable {

        private String appName;

        private String host;

        private int port;

        ConnectRunner(String appName, String host, int port) {
            this.appName = appName;
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {
            // The Server
            server = new Server(new ExecutorThreadPool()); // 非阻塞

            // HTTP connector
            ServerConnector connector = new ServerConnector(server);
            if (host != null && host.trim().length() > 0) {
                connector.setHost(host);
            }
            connector.setPort(port);
            server.setConnectors(new Connector[] { connector });

            // Set a handler
            HandlerCollection handlerc = new HandlerCollection();
            handlerc.setHandlers(new Handler[] { new JettyServerHandler() });
            server.setHandler(handlerc);

            try {
                // Start server
                server.start();
                LOGGER.info(">>>>>>>>>>>> xxl-job jetty server start success at port:{}.", port);

                // Start Registry-Server
                ExecutorRegistryThread.getInstance().start(port, host, appName);

                // Start Callback-Server
                TriggerCallbackThread.getInstance().start();

                server.join(); // block until thread stopped
                LOGGER.info(">>>>>>>>>>> xxl-rpc server join success, netcon={}, port={}", JettyServer.class.getName(),
                        port);
            } catch (Exception e) {
                LOGGER.error("构建本地监听服务失败", e);
            }
        }
    }

}
