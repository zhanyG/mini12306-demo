package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Train {
    private Long id;
    private String trainNumber;
    private String startStation;
    private String endStation;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Double price;
    private Integer totalSeats = 100;
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
