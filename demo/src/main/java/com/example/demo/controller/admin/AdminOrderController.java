package com.example.demo.controller.admin;

import com.example.demo.entity.Order;
import com.example.demo.request.AdminOrderCreateRequest;
import com.example.demo.request.AdminOrderUpdateRequest;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminService adminService;

    public AdminOrderController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<Order> list() {
        return adminService.getAllOrders();
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return adminService.getOrderById(id);
    }

    @PostMapping
    public Order create(@Valid @RequestBody AdminOrderCreateRequest request) {
        return adminService.createOrder(request);
    }

    @PutMapping("/{id}")
    public Order update(@PathVariable Long id, @Valid @RequestBody AdminOrderUpdateRequest request) {
        return adminService.updateOrder(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        adminService.deleteOrder(id);
        return Map.of("message", "删除成功");
    }
}
