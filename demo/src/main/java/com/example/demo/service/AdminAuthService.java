package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.request.LoginRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 管理员认证服务：校验 ADMIN 角色并签发 token。
 * token 存储在内存中，适用于测试/演示环境。
 */
@Service
public class AdminAuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Map<String, Long> tokenStore = new ConcurrentHashMap<>();

    public AdminAuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (!"ADMIN".equals(user.getRole())) {
            throw new RuntimeException("无管理员权限");
        }
        String token = UUID.randomUUID().toString();
        tokenStore.put(token, user.getId());
        return Map.of(
                "token", token,
                "userId", user.getId(),
                "username", user.getUsername()
        );
    }

    public boolean validateToken(String token) {
        return token != null && tokenStore.containsKey(token);
    }

    public Long getUserIdByToken(String token) {
        return tokenStore.get(token);
    }

    public void logout(String token) {
        if (token != null) {
            tokenStore.remove(token);
        }
    }
}
