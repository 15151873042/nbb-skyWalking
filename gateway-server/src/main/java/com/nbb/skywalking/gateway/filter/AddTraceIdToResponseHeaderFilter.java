package com.nbb.skywalking.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.apache.skywalking.apm.toolkit.webflux.WebFluxSkyWalkingOperators;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


/**
 * 将traceId添加到response响应头
 */
@Slf4j
@Component
public class AddTraceIdToResponseHeaderFilter implements GlobalFilter, Ordered {

    /**
     * spring-cloud-gateway基于netty，无法再日志中打印tranceID
     * @link https://github.com/apache/skywalking/issues/10509#issuecomment-1752263313
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String traceId = WebFluxSkyWalkingOperators.continueTracing(exchange, TraceContext::traceId);
        MDC.put("mdcTracdId", traceId);

        log.info("网关被调用了， traceId为：{}", traceId);
        exchange.getResponse().getHeaders().set("x-trace-id", traceId);
        return chain.filter(exchange)
                .then(Mono.fromRunnable(() -> MDC.remove("mdcTracdId")));
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }
}
