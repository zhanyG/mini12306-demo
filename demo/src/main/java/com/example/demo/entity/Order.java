package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private String orderNo;
    private Long userId;
    private Long trainId;
    private Long passengerId;
    private String status; // 未支付 / 已支付 / 已出票 / 已退票
    private Double price;
    private LocalDateTime createTime;

    public Order(Long userId, Long trainId, Long passengerId, Double price) {
        this.userId = userId;
        this.trainId = trainId;
        this.passengerId = passengerId;
        this.price = price;
        this.status = "未支付";
        this.createTime = LocalDateTime.now();
        this.orderNo = "ORD" + System.currentTimeMillis();
    }

    public void pay() { this.status = "已支付"; }
    public void confirm() { this.status = "已出票"; }
    public void cancel() { this.status = "已退票"; }
}
