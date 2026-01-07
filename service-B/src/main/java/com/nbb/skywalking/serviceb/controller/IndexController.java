package com.nbb.skywalking.serviceb.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class IndexController {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    @RequestMapping( "/test/{str}")
    public String echo(@PathVariable String str) {
        String traceId = TraceContext.traceId();
        System.out.println(traceId);
        log.info("====我是service-B服务，我被调用了，传入参数是：{}====", str);
//        // 模拟出错
//        Object o = redisTemplate.opsForValue().get(str);
//        System.out.println(o.toString());
        return str;
    }


    @Scheduled(cron = "0/5 * * * * ?") // 每10秒执行
    public void testJob() throws NoSuchFieldException, IllegalAccessException, ClassNotFoundException {
        String traceId = TraceContext.traceId();
        // 日志中会自动打印 traceId（如果日志配置了 %X{traceId}）
        log.info("定时任务执行，traceId：{}", traceId);
    }
}
