package com.nbb.skywalking.servicea.filter;

import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 将traceId添加到response header中
 *
 * @author 胡鹏
 */
public class TraceFilter extends OncePerRequestFilter  {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // 设置响应 traceId
        response.addHeader("trace-id", TraceContext.traceId());
        // 继续过滤
        filterChain.doFilter(request, response);
    }
}
