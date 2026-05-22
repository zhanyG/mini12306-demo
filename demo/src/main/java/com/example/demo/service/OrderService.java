package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class OrderService {

    private final TrainRepository trainRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;

    public OrderService(TrainRepository trainRepository,
                        OrderRepository orderRepository,
                        TicketRepository ticketRepository) {
        this.trainRepository = trainRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
    }

    public Order buyTicket(Long userId, Long trainId, Long passengerId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("车次不存在"));
        if (!train.bookSeat()) {
            throw new RuntimeException("余票不足");
        }
        trainRepository.save(train);

        Order order = new Order(userId, trainId, passengerId, train.getPrice());
        order.pay();
        order.confirm();
        order = orderRepository.save(order);

        Ticket ticket = new Ticket(order.getId(), passengerId, "01车01A");
        ticketRepository.save(ticket);

        return order;
    }

    public boolean cancelTicket(Long orderId) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order == null || !"已出票".equals(order.getStatus())) {
            return false;
        }

        Train train = trainRepository.findById(order.getTrainId()).orElse(null);
        if (train != null) {
            train.refundSeat();
            trainRepository.save(train);
        }

        order.cancel();
        orderRepository.save(order);

        Ticket ticket = ticketRepository.findByOrderId(orderId).orElse(null);
        if (ticket != null) {
            ticket.refund();
            ticketRepository.save(ticket);
        }

        return true;
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
