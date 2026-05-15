package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.DataService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DataService dataService;

    public UserController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        // 校验必填字段
        if (user.getUsername() == null || user.getUsername().isBlank()) {
            throw new RuntimeException("用户名不能为空");
        }
        user.setId(dataService.nextUserId());
        dataService.saveUser(user);
        return user;
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        return dataService.getUsers().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst().orElse(null);
    }
}