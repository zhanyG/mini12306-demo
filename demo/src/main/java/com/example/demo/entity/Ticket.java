package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Ticket {
    private Long id;
    private String ticketNo;
    private Long orderId;
    private Long passengerId;
    private String seatNumber;
    private String seatType = "二等座";
    private String status = "有效";
    private LocalDateTime issueTime;

    public Ticket(Long orderId, Long passengerId, String seatNumber) {
        this.orderId = orderId;
        this.passengerId = passengerId;
        this.seatNumber = seatNumber;
        this.ticketNo = "TKT" + System.currentTimeMillis();
        this.issueTime = LocalDateTime.now();
    }

    public void refund() { this.status = "已退"; }
}
