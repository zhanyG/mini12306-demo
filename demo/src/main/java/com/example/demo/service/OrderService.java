package com.example.demo.service;

import com.example.demo.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final DataService dataService;

    public OrderService(DataService dataService) {
        this.dataService = dataService;
    }

    // 购票：创建订单 + 扣减余票 + 生成车票
    public Order buyTicket(Long userId, Long trainId, Long passengerId) {
        Train train = dataService.getTrains().stream()
                .filter(t -> t.getId().equals(trainId))
                .findFirst().orElse(null);
        if (train == null || !train.bookSeat()) {
            throw new RuntimeException("余票不足或车次不存在");
        }

        // 创建订单
        Order order = new Order(userId, trainId, passengerId, train.getPrice());
        order.setId(dataService.nextOrderId());
        dataService.saveOrder(order);

        // 生成车票（固定座位号：01车01A）
        Ticket ticket = new Ticket(order.getId(), passengerId, "01车01A");
        ticket.setId(dataService.nextTicketId());
        dataService.saveTicket(ticket);

        // 订单状态流转
        order.pay();
        order.confirm();

        return order;
    }

    // 退票：恢复余票 + 更新订单/车票状态
    public boolean cancelTicket(Long orderId) {
        Order order = dataService.getOrders().stream()
                .filter(o -> o.getId().equals(orderId))
                .findFirst().orElse(null);
        if (order == null || !"已出票".equals(order.getStatus())) {
            return false;
        }

        // 找到对应列车并恢复座位
        Train train = dataService.getTrains().stream()
                .filter(t -> t.getId().equals(order.getTrainId()))
                .findFirst().orElse(null);
        if (train != null) {
            train.refundSeat();
        }

        // 更新状态
        order.cancel();
        Ticket ticket = dataService.getTickets().stream()
                .filter(t -> t.getOrderId().equals(orderId))
                .findFirst().orElse(null);
        if (ticket != null) {
            ticket.refund();
        }

        return true;
    }

    // 查询用户订单（简化版）
    public List<Order> getUserOrders(Long userId) {
        return dataService.getOrders().stream()
                .filter(o -> o.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
