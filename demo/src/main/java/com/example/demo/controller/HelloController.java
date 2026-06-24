package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** 初始示例接口，用于验证服务是否正常启动。 */
@RestController
public class HelloController {

    @GetMapping("/hello")
    public String sayHello() {
        return "你好！这是你的第一个 Spring Boot 接口！";
    }
}
