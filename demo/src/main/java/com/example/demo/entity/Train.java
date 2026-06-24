package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 车次实体，映射 trains 表。
 * 每趟车次包含基础信息（车号、起止站、时间、价格）和座位库存（总座位数、余票数）。
 */
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

    /**
     * 尝试预订一个座位（扣减余票）。
     * @return 有余票时 true，售罄时 false
     */
    public boolean bookSeat() {
        if (availableSeats > 0) {
            availableSeats--;
            return true;
        }
        return false;
    }

    /**
     * 退还一个座位（退票时释放余票，上限不超过总座位数）。
     */
    public void refundSeat() {
        if (availableSeats < totalSeats) availableSeats++;
    }
}