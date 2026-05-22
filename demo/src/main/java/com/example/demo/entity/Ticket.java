package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

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

    @Column(name = "seat_type", length = 20)
    private String seatType = "二等座";

    @Column(name = "status", length = 20)
    private String status = "有效";

    @Column(name = "issue_time")
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
