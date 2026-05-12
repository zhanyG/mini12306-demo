package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // 1. 告诉 Spring 这是一个处理 HTTP 请求的类
public class HelloController {

    @GetMapping("/hello") // 2. 定义路径：访问 http://localhost:8080/hello 时会执行这个方法
    public String sayHello() {
        return "你好！这是你的第一个 Spring Boot 接口！";
    }
}
