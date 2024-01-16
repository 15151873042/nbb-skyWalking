package com.nbb.skywalking.serviceb.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class IndexController {

    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;


    @RequestMapping( "/{str}")
    public String echo(@PathVariable String str) {
        log.info("====我是service-B服务，我被调用了，传入参数是：{}====", str);
//        // 模拟出错
//        Object o = redisTemplate.opsForValue().get(str);
//        System.out.println(o.toString());
        return str;
    }
}
