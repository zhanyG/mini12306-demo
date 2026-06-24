package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 车票实体，映射 tickets 表。
 * 每张车票关联一个订单和一位乘客，出票时自动生成票号和出票时间。
 */
@Entity
@Table(name = "tickets")
@Data
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_no", length = 50)
    private String ticketNo;

    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "passenger_id")
    private Long passengerId;

    @Column(name = "seat_number", length = 20)
    private String seatNumber;

    /** 座位类型：二等座 / 一等座 / 商务座 */
    @Column(name = "seat_type", length = 20)
    private String seatType = "二等座";

    /** 车票状态：有效 / 已退 */
    @Column(name = "status", length = 20)
    private String status = "有效";

    @Column(name = "issue_time")
    private LocalDateTime issueTime;

    /** 出票时自动生成票号和出票时间 */
    public Ticket(Long orderId, Long passengerId, String seatNumber) {
        this.orderId = orderId;
        this.passengerId = passengerId;
        this.seatNumber = seatNumber;
        this.ticketNo = "TKT" + System.currentTimeMillis();
        this.issueTime = LocalDateTime.now();
    }

    /** 退票：将车票状态标记为已退 */
    public void refund() { this.status = "已退"; }
}
