package com.nbb.skywalking.servicea.controller;


import lombok.extern.slf4j.Slf4j;
import org.apache.skywalking.apm.toolkit.trace.RunnableWrapper;
import org.apache.skywalking.apm.toolkit.trace.TraceCrossThread;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Slf4j
public class IndexController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping( "/{str}")
    public String echo(@PathVariable String str) {
//        ActiveSpan.setOperationName("serviceA操作名称"); // 设置当前端点的名称， 也可以使用@Trace注解
//        ActiveSpan.tag("mainThread", "started"); // 设置端点被调用时候的自定义元数据信息，也可使用@Tags注解
//        ActiveSpan.info("Service A 被调用"); // 设置当前端点调用的日志信息

        log.info("====我是service-A服务，准备调用service-B服务，传入参数是：{}====", str);

        // 第一次调用
        String result1 = restTemplate.getForObject("http://service-B-app/" + str, String.class);

        // 异步线程调用可通过@TraceCrossThread，将Trace-id传递到子线程（异步线程手动MDC设置是无效的，虽然日志打印了traceId，skywalking-apo上依旧没有traceId）
        // https://skywalking.apache.org/docs/skywalking-java/latest/en/setup/service-agent/java-agent/application-toolkit-trace-cross-thread/
        new Thread(RunnableWrapper.of(() -> {
            String result2 = restTemplate.getForObject("http://service-B-app/" + str + str, String.class);
            log.info("====第二次调用service-B服务，返回结果是：{}====", result2);
        })).start();

        return result1;
    }
}
