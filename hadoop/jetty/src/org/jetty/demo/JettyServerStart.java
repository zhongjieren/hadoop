package org.jetty.demo;
//启动类，入口
public class JettyServerStart {
    public static void main(String[] args) {
        JettyCustomServer server = new JettyCustomServer(
                "./jetty/etc/jetty.xml", "/testContext");
        server.startServer();

    }
}

 