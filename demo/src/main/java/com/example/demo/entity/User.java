package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "real_name", length = 50)
    private String realName;

    @Column(name = "id_card", length = 18)
    private String idCard;

    @Column(name = "create_time")
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