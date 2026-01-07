package com.nbb.skywalking.servicea.controller;


import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.apache.skywalking.apm.toolkit.trace.TraceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RestController
@Slf4j
public class IndexController {

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 同步接口调用，测试traceId
     */
    @RequestMapping("/sync/{str}")
    public String syncCall(@PathVariable String str) {
//        ActiveSpan.setOperationName("serviceA操作名称"); // 设置当前端点的名称， 也可以使用@Trace注解
//        ActiveSpan.tag("mainThread", "started"); // 设置端点被调用时候的自定义元数据信息，也可使用@Tags注解
//        ActiveSpan.info("Service A 被调用"); // 设置当前端点调用的日志信息
        log.info("====我是service-A服务，同步调用service-B服务，传入参数是：{}====", str);
        String result = restTemplate.getForObject("http://service-B-app/test/" + str, String.class);
        log.info("====调用service-B服务，返回结果是：{}====", result);
        return result;
    }


    /**
     * 异步接口调用，测试traceId
     */
    @RequestMapping("/async/{str}")
    public String asyncCall(@PathVariable String str) {
        log.info("====我是service-A服务，异步调用service-B服务，传入参数是：{}====", str);
        // 1、如果异步任务想要输出Trace-id，只需要通过@TraceCrossThread修饰Runnable即可；skywalking提供了RunnableWrapper.of方法来包装Runnable，包装之后的任务运行时即可打印traceId
        // https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/application-toolkit-trace-cross-thread/
        // tips:异步线程手动MDC设置是无效的，虽然日志打印了traceId，skywalking-apo上依旧没有traceId
        new Thread(RunnableWrapper.of(() -> {
            String result = restTemplate.getForObject("http://service-B-app/test/" + str, String.class);
            log.info("====调用service-B服务，返回结果是：{}====", result);
        })).start();

        return "success";
    }


    /**
     * 非skywalking plugin支持，测试traceId
     * 由于skywalking没有xxl-job的plugin，xxl-job定时任务执行的时候，是不会产生traceI的，
     * 这边添加@Trace注解可以用于标记需要被链路追踪的方法，当xxl-job任务执行时候，对应的线程就产生traceId了
     */
    @Trace
    @XxlJob("demoJobHandler")
    public void demoJobHandler() {
        String traceId = TraceContext.traceId();
        log.info("XXL-Job任务执行，SkyWalking TraceID: {}", traceId);
        String result = restTemplate.getForObject("http://service-B-app/test/" + System.currentTimeMillis(), String.class);
        log.info("xxl-job定时任务调用Service-B服务的返回值为：{}", result);
    }


    /**
     * mq收到消息，手动添加traceId
     * 通过xxl-job定时任务模拟收到消息
     * xxl-job定时任务执行时，任务参数填写如下格式：3283c5c46c1d4f07976f129dcaa7b827.301.17677766059490050
     */
    @XxlJob("demoJobHandler2")
    public void receiveMessage() throws Exception {
        // 模拟从mq消息体中获取traceId
        String customTraceId = XxlJobHelper.getJobParam();
        // 手动设置traceId
        this.setCustomTraceId(customTraceId);


        String traceId = TraceContext.traceId();
        log.info("XXL-Job任务执行，SkyWalking TraceID: {}", traceId);
        String result = restTemplate.getForObject("http://service-B-app/test/" + System.currentTimeMillis(), String.class);
        log.info("xxl-job定时任务调用Service-B服务的返回值为：{}", result);

        // 业务代码执行之后，移除traceId
        this.removeCustomTraceId();
    }


    private void setCustomTraceId(String traceId) throws Exception {
        Class<?> clazz = Class.forName("org.apache.skywalking.apm.agent.core.context.ContextCarrier");
        Object obj = clazz.getConstructor().newInstance();

        Method setTraceId = clazz.getDeclaredMethod("setTraceId", String.class);
        setTraceId.setAccessible(true);
        setTraceId.invoke(obj, traceId);

        /** 以下内容必须要设置，否则，会自动生成一个新的traceId（原因是内部ContextCarrier#isValid()方法会校验对应的值，如果不对，就会从新生成一个） */
        Field traceSegmentId = clazz.getDeclaredField("traceSegmentId");
        traceSegmentId.setAccessible(true);
        traceSegmentId.set(obj, "自定义设置traceId");
        Field spanId = clazz.getDeclaredField("spanId");
        spanId.setAccessible(true);
        spanId.set(obj, 2000);
        Field parentService = clazz.getDeclaredField("parentService");
        parentService.setAccessible(true);
        parentService.set(obj, "300");
        Field parentServiceInstance = clazz.getDeclaredField("parentServiceInstance");
        parentServiceInstance.setAccessible(true);
        parentServiceInstance.set(obj, "400");
        Field parentEndpoint = clazz.getDeclaredField("parentEndpoint");
        parentEndpoint.setAccessible(true);
        parentEndpoint.set(obj, "500");
        Field addressUsedAtClient = clazz.getDeclaredField("addressUsedAtClient");
        addressUsedAtClient.setAccessible(true);
        addressUsedAtClient.set(obj, "600");


        Class<?> contextManagerClass = Class.forName("org.apache.skywalking.apm.agent.core.context.ContextManager");
        Method createEntrySpanMethod = contextManagerClass.getDeclaredMethod("createEntrySpan", String.class, clazz);
        createEntrySpanMethod.setAccessible(true);
        createEntrySpanMethod.invoke(null, "demoJobHandler", obj);
    }

    private void removeCustomTraceId() throws Exception {
        Class<?> contextManagerClass = Class.forName("org.apache.skywalking.apm.agent.core.context.ContextManager");
        Method stopSpanMethod = contextManagerClass.getDeclaredMethod("stopSpan");
        stopSpanMethod.invoke(null);
    }
}
