package com.example.demo.request;

import jakarta.validation.constraints.NotNull;

/** 管理员代客下单请求 DTO */
public class AdminOrderCreateRequest {

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    @NotNull(message = "车次ID不能为空")
    private Long trainId;

    @NotNull(message = "乘客ID不能为空")
    private Long passengerId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getTrainId() { return trainId; }
    public void setTrainId(Long trainId) { this.trainId = trainId; }
    public Long getPassengerId() { return passengerId; }
    public void setPassengerId(Long passengerId) { this.passengerId = passengerId; }
}
