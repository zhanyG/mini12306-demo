package com.example.demo.controller;

import com.example.demo.entity.Order;
import com.example.demo.service.OrderService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 订单相关 API：购票、退票、改签、查询用户订单。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /** 购票（一步完成下单+支付+出票） */
    @PostMapping("/buy")
    public Order buyTicket(
            @RequestParam Long userId,
            @RequestParam Long trainId,
            @RequestParam Long passengerId) {
        return orderService.buyTicket(userId, trainId, passengerId);
    }

    /** 创建订单（未支付） */
    @PostMapping("/create")
    public Order createOrder(
            @RequestParam Long userId,
            @RequestParam Long trainId,
            @RequestParam Long passengerId) {
        return orderService.createOrder(userId, trainId, passengerId);
    }

    /** 模拟支付 */
    @PostMapping("/{orderId}/simulate-pay")
    public Order simulatePay(@PathVariable Long orderId) {
        return orderService.simulatePay(orderId);
    }

    /** 确认支付（第三方支付回调后调用） */
    @PostMapping("/{orderId}/confirm-pay")
    public Order confirmPay(@PathVariable Long orderId,
                            @RequestParam String payType,
                            @RequestParam String payTradeNo) {
        return orderService.confirmPayment(orderId, payType, payTradeNo);
    }

    /** 退票 */
    @PostMapping("/{orderId}/cancel")
    public String cancelOrder(@PathVariable Long orderId) {
        boolean success = orderService.cancelTicket(orderId);
        return success ? "退票成功" : "退票失败：订单无效或已处理";
    }

    /** 改签 */
    @PostMapping("/{orderId}/reschedule")
    public Order rescheduleOrder(@PathVariable Long orderId, @RequestParam Long newTrainId) {
        return orderService.rescheduleTicket(orderId, newTrainId);
    }

    /** 改签补差价支付（用于新车次价格更高时） */
    @PostMapping("/{orderId}/pay-upgrade")
    public Order payUpgrade(@PathVariable Long orderId, @RequestParam Long newTrainId) {
        return orderService.payUpgrade(orderId, newTrainId);
    }

    /** 根据 ID 查询订单 */
    @GetMapping("/{orderId}")
    public Order getOrder(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId);
    }

    /** 查询用户的订单列表 */
    @GetMapping("/user/{userId}")
    public List<Order> getUserOrders(@PathVariable Long userId) {
        return orderService.getUserOrders(userId);
    }
}
