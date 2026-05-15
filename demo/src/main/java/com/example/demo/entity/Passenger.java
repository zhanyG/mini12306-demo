package com.example.demo.entity;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Passenger {
    private Long id;
    private Long userId;
    private String name;
    private String idCard;
    private String phone;

    public Passenger(Long userId, String name, String idCard) {
        this.userId = userId;
        this.name = name;
        this.idCard = idCard;
    }
}
