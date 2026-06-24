package com.example.demo.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDateTime;

/** 管理员创建/更新车次请求 DTO */
public class TrainRequest {

    @NotBlank(message = "车次号不能为空")
    private String trainNumber;

    @NotBlank(message = "始发站不能为空")
    private String startStation;

    @NotBlank(message = "终点站不能为空")
    private String endStation;

    @NotNull(message = "出发时间不能为空")
    private LocalDateTime departureTime;

    @NotNull(message = "到达时间不能为空")
    private LocalDateTime arrivalTime;

    @NotNull(message = "票价不能为空")
    @Positive(message = "票价必须大于0")
    private Double price;

    @Positive(message = "总座位数必须大于0")
    private Integer totalSeats = 100;

    public String getTrainNumber() { return trainNumber; }
    public void setTrainNumber(String trainNumber) { this.trainNumber = trainNumber; }
    public String getStartStation() { return startStation; }
    public void setStartStation(String startStation) { this.startStation = startStation; }
    public String getEndStation() { return endStation; }
    public void setEndStation(String endStation) { this.endStation = endStation; }
    public LocalDateTime getDepartureTime() { return departureTime; }
    public void setDepartureTime(LocalDateTime departureTime) { this.departureTime = departureTime; }
    public LocalDateTime getArrivalTime() { return arrivalTime; }
    public void setArrivalTime(LocalDateTime arrivalTime) { this.arrivalTime = arrivalTime; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Integer getTotalSeats() { return totalSeats; }
    public void setTotalSeats(Integer totalSeats) { this.totalSeats = totalSeats; }
}
