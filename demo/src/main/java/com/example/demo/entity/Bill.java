package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 账单实体，映射 bills 表。
 * 独立记录每笔资金流水：购票付款、退票退款、改签差价。
 */
@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
public class Bill {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "bill_no", length = 50)
    private String billNo;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "user_id")
    private Long userId;

    /** 金额（始终为正数，方向由 type 区分） */
    @Column(name = "amount")
    private Double amount;

    /** 账单类型：PAYMENT / REFUND / CHANGE_UPGRADE / CHANGE_REFUND */
    @Column(name = "type", length = 20)
    private String type;

    /** 状态：已完成 */
    @Column(name = "status", length = 20)
    private String status;

    /** 描述信息，如车次和线路 */
    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    public Bill(Long orderId, Long userId, Double amount, String type, String description) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.status = "已完成";
        this.createTime = LocalDateTime.now();
        this.billNo = "BILL" + System.currentTimeMillis();
    }
}
