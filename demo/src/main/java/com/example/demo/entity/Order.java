package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 订单实体，映射 orders 表。
 * 订单生命周期：未支付 → 已支付 → 已出票 / 已退票。
 */
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

    /** 订单状态：未支付 / 已支付 / 已出票 / 已退票 */
    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "price")
    private Double price;

    /** 支付方式：WECHAT / ALIPAY / CASH */
    @Column(name = "pay_type", length = 20)
    private String payType;

    /** 第三方支付交易号 */
    @Column(name = "pay_trade_no", length = 100)
    private String payTradeNo;

    /** 支付时间 */
    @Column(name = "pay_time")
    private LocalDateTime payTime;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    /** 创建订单时自动生成订单号并设为"未支付"状态 */
    public Order(Long userId, Long trainId, Long passengerId, Double price) {
        this.userId = userId;
        this.trainId = trainId;
        this.passengerId = passengerId;
        this.price = price;
        this.status = "未支付";
        this.createTime = LocalDateTime.now();
        this.orderNo = "ORD" + System.currentTimeMillis();
    }

    /** 标记为已支付 */
    public void pay() { this.status = "已支付"; }
    /** 标记为已出票 */
    public void confirm() { this.status = "已出票"; }
    /** 标记为已退票 */
    public void cancel() { this.status = "已退票"; }
    /** 标记为已改签 */
    public void change() { this.status = "已改签"; }
}
