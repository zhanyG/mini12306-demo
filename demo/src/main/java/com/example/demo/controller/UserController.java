package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.DataService;
import com.example.demo.request.LoginRequest;
import com.example.demo.util.IdCardValidator;
import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import org.springframework.http.ResponseEntity;

/**
 * 用户相关 API：注册、登录、查询、修改个人信息、修改密码。
 * 密码使用 BCrypt 加密存储。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final DataService dataService;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserController(DataService dataService) {
        this.dataService = dataService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /** 用户注册（密码 BCrypt 加密后入库） */
    @PostMapping("/register")
    public User register(@Valid @RequestBody User user) {
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (dataService.getUserByUsername(user.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(java.time.LocalDateTime.now());
        return dataService.saveUser(user);
    }

    /** 根据 ID 查询用户 */
    @GetMapping("/{id}")
    public User getUser(@PathVariable Long id) {
        User user = dataService.getUserById(id);
        if (user == null) throw new RuntimeException("用户不存在");
        return user;
    }

    /**
     * 用户登录：校验用户名和 BCrypt 密码，成功返回 token。
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        User user = dataService.getUserByUsername(request.getUsername());
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).body(Map.of("error", "用户名或密码错误"));
        }
        String token = java.util.UUID.randomUUID().toString();
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("userId", user.getId());
        response.put("username", user.getUsername());
        return ResponseEntity.ok(response);
    }

    /** 实名认证：校验姓名 + 身份证号码合法性，通过后保存到用户 */
    @PostMapping("/{id}/verify-idcard")
    public ResponseEntity<Map<String, Object>> verifyIdCard(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = dataService.getUserById(id);
        if (user == null) throw new RuntimeException("用户不存在");

        String realName = body.get("realName");
        String idCard = body.get("idCard");

        if (realName == null || realName.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请填写真实姓名"));
        }
        if (idCard == null || idCard.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "请填写身份证号码"));
        }

        String error = IdCardValidator.validate(idCard.trim().toUpperCase());
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error));
        }

        user.setRealName(realName.trim());
        user.setIdCard(idCard.trim().toUpperCase());
        dataService.saveUser(user);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "实名认证成功");
        result.put("birthDate", IdCardValidator.extractBirthDate(idCard));
        result.put("gender", IdCardValidator.extractGender(idCard));
        return ResponseEntity.ok(result);
    }

    /** 修改个人信息（手机号、真实姓名、身份证号） */
    @PutMapping("/{id}")
    public User updateProfile(@PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = dataService.getUserById(id);
        if (user == null) throw new RuntimeException("用户不存在");

        if (body.containsKey("phone")) user.setPhone(body.get("phone"));
        if (body.containsKey("realName")) user.setRealName(body.get("realName"));
        if (body.containsKey("idCard")) user.setIdCard(body.get("idCard"));

        return dataService.saveUser(user);
    }

    /** 修改密码（校验旧密码） */
    @PutMapping("/{id}/password")
    public ResponseEntity<Map<String, Object>> changePassword(
            @PathVariable Long id, @RequestBody Map<String, String> body) {
        User user = dataService.getUserById(id);
        if (user == null) throw new RuntimeException("用户不存在");

        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        if (oldPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "请提供旧密码和新密码"));
        }
        if (newPassword.length() < 6) {
            return ResponseEntity.badRequest().body(Map.of("error", "新密码长度不能少于6位"));
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            return ResponseEntity.status(400).body(Map.of("error", "旧密码错误"));
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        dataService.saveUser(user);
        return ResponseEntity.ok(Map.of("message", "密码修改成功"));
    }
}