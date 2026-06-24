package com.example.demo.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

/** 管理员更新订单请求 DTO */
public class AdminOrderUpdateRequest {

    @Pattern(regexp = "未支付|已支付|已出票|已退票", message = "无效的订单状态")
    private String status;

    @Positive(message = "金额必须大于0")
    private Double price;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
}
