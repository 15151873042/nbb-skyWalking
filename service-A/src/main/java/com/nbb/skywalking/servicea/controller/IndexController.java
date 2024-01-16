package com.nbb.skywalking.servicea.controller;


import lombok.extern.slf4j.Slf4j;
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
        log.info("====我是service-A服务，准备调用service-B服务，传入参数是：{}====", str);
        return restTemplate.getForObject("http://service-B-app/" + str, String.class);
    }
}
