package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@Data
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_no", length = 50)
    private String orderNo;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "train_id")
    private Long trainId;

    @Column(name = "passenger_id")
    private Long passengerId;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "price")
    private Double price;

    @Column(name = "create_time")
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
