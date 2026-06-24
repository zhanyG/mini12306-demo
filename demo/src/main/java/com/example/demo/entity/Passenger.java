package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 常用乘客实体，映射 passengers 表。
 * 一个用户可以添加多个常用乘客，购票时为乘客购票。
 */
@Entity
@Table(name = "passengers")
@Data
@NoArgsConstructor
public class Passenger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 所属用户 ID */
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "name", length = 50)
    private String name;

    @Column(name = "id_card", length = 18)
    private String idCard;

    @Column(name = "phone", length = 20)
    private String phone;

    public Passenger(Long userId, String name, String idCard) {
        this.userId = userId;
        this.name = name;
        this.idCard = idCard;
    }
}
