package com.example.demo.service;

import com.example.demo.entity.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class DataService {
    private final List<User> users = new ArrayList<>();
    private final List<Passenger> passengers = new ArrayList<>();
    private final List<Train> trains = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final List<Ticket> tickets = new ArrayList<>();

    private final AtomicLong userIdSeq = new AtomicLong(1L);
    private final AtomicLong passengerIdSeq = new AtomicLong(1L);
    private final AtomicLong trainIdSeq = new AtomicLong(1L);
    private final AtomicLong orderIdSeq = new AtomicLong(1L);
    private final AtomicLong ticketIdSeq = new AtomicLong(1L);

    // 初始化测试数据（启动时自动加载）
    public DataService() {
        // 添加1个测试列车
        Train t1 = new Train("G1001", "北京南", "上海虹桥",
                LocalDateTime.of(2025, 4, 6, 8, 0),
                LocalDateTime.of(2025, 4, 6, 12, 30), 553.0);
        t1.setId(trainIdSeq.getAndIncrement());
        trains.add(t1);
    }

    // Getters（供Controller调用）
    public List<User> getUsers() { return users; }
    public List<Train> getTrains() { return trains; }
    public List<Order> getOrders() { return orders; }
    public List<Ticket> getTickets() { return tickets; }

    // 生成ID
    public Long nextUserId() { return userIdSeq.getAndIncrement(); }
    public Long nextPassengerId() { return passengerIdSeq.getAndIncrement(); }
    public Long nextOrderId() { return orderIdSeq.getAndIncrement(); }
    public Long nextTicketId() { return ticketIdSeq.getAndIncrement(); }

    // 保存
    public void saveUser(User user) { users.add(user); }
    public void savePassenger(Passenger p) { passengers.add(p); }
    public void saveOrder(Order o) { orders.add(o); }
    public void saveTicket(Ticket t) { tickets.add(t); }
}
