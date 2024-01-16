package com.nbb.skywalking.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayServerApplication {


    /**
     * 项目启动，添加如下参数，用于接入skyWalking agent
     * <pre>
     * -javaagent:E:\server\skywalking\apache-skywalking-apm-bin-es7\agent\skywalking-agent.jar
     * -Dskywalking.agent.service_name=gateway-server-app
     * -Dskywalking.collector.backend_service=127.0.0.1:11800
     * </pre>
     * @param args
     */
    public static void main(String[] args) {
        SpringApplication.run(GatewayServerApplication.class, args);
    }
}
