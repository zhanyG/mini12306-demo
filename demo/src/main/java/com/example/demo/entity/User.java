package com.example.demo.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class User {
    private Long id;
    private String username;
    private String password;
    private String phone;
    private String realName;
    private String idCard;
    private LocalDateTime createTime;

    public User(String username, String password, String phone, String realName, String idCard) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.realName = realName;
        this.idCard = idCard;
        this.createTime = LocalDateTime.now();
    }
}