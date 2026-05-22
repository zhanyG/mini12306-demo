package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trains")
@Data
@NoArgsConstructor
public class Train {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "train_number", length = 20)
    private String trainNumber;

    @Column(name = "start_station", length = 50)
    private String startStation;

    @Column(name = "end_station", length = 50)
    private String endStation;

    @Column(name = "departure_time")
    private LocalDateTime departureTime;

    @Column(name = "arrival_time")
    private LocalDateTime arrivalTime;

    @Column(name = "price")
    private Double price;

    @Column(name = "total_seats")
    private Integer totalSeats = 100;

    @Column(name = "available_seats")
    private Integer availableSeats = 100;

    public Train(String trainNumber, String startStation, String endStation,
                 LocalDateTime departureTime, LocalDateTime arrivalTime, Double price) {
        this.trainNumber = trainNumber;
        this.startStation = startStation;
        this.endStation = endStation;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.price = price;
    }

    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    public void refundSeat() {
        if (availableSeats < totalSeats) availableSeats++;
    }
}