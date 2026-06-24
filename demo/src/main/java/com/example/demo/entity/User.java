package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 用户实体，映射 users 表。
 * 用户注册后可添加常用乘客、购票和管理订单。
 */
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

    /** 写入时接收密码，读取时忽略（注册时前端传密码，但查询用户不返回密码） */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    /** 角色：USER 普通用户 / ADMIN 管理员（仅服务端可写） */
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "role", length = 20)
    private String role = "USER";

    /** 注册时创建用户，自动记录创建时间 */
    public User(String username, String password, String phone, String realName, String idCard) {
        this.username = username;
        this.password = password;
        this.phone = phone;
        this.realName = realName;
        this.idCard = idCard;
        this.createTime = LocalDateTime.now();
        this.role = "USER";
    }
}