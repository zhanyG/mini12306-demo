package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.DataService;
import org.springframework.web.bind.annotation.*;
import com.example.demo.request.LoginRequest;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;

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

    /* ai生成的登录方法，引入类LoginRequest,方法getUserByUsername */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody LoginRequest request) {
        // 输入校验
        if (request.getUsername() == null || request.getPassword() == null ||
                request.getUsername().isBlank() || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "用户名或密码不能为空"));
        }
        // 查找用户（注意：这里假设 password 是明文存储 —— 文档里没加密，所以暂时这样）
        User user = dataService.getUserByUsername(request.getUsername());
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        // ✅ 简单比对密码（生产环境请加密！）
        if (!request.getPassword().equals(user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        // ✅ 成功登录：生成一个临时 session token（字符串即可，比如用 UUID）
        String token = java.util.UUID.randomUUID().toString();
        // ✅ 构造响应（不含密码）
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }
}