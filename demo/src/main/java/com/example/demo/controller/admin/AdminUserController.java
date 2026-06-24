package com.example.demo.controller.admin;

import com.example.demo.entity.User;
import com.example.demo.request.AdminUserRequest;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminService adminService;

    public AdminUserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<User> list() {
        return adminService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable Long id) {
        return adminService.getUserById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody AdminUserRequest request) {
        return adminService.createUser(request);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id, @Valid @RequestBody AdminUserRequest request) {
        return adminService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        adminService.deleteUser(id);
        return Map.of("message", "删除成功");
    }
}
