package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }
    ..
    @PostMapping("/buy")
    public Order buyTicket(
            @RequestParam Long userId,
            @RequestParam Long trainId,
            @RequestParam Long passengerId) {
        return orderService.buyTicket(userId, trainId, passengerId);
    }

    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        boolean success = orderService.cancelTicket(orderId);
        return success ? "退票成功" : "退票失败：订单无效或已处理";
    }

    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }
}
