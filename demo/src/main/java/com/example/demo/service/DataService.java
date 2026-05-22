package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataService {

    private final UserRepository userRepository;
    private final TrainRepository trainRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final PassengerRepository passengerRepository;

    public DataService(UserRepository userRepository,
                       TrainRepository trainRepository,
                       OrderRepository orderRepository,
                       TicketRepository ticketRepository,
                       PassengerRepository passengerRepository) {
        this.userRepository = userRepository;
        this.trainRepository = trainRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.passengerRepository = passengerRepository;
    }

    @PostConstruct
    public void initTestData() {
        if (trainRepository.count() == 0) {
            Train t1 = new Train("G1001", "北京南", "上海虹桥",
                    LocalDateTime.of(2025, 4, 6, 8, 0),
                    LocalDateTime.of(2025, 4, 6, 12, 30), 553.0);
            trainRepository.save(t1);
        }
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public List<User> getUsers() { return userRepository.findAll(); }
    public List<Train> getTrains() { return trainRepository.findAll(); }
    public List<Order> getOrders() { return orderRepository.findAll(); }
    public List<Ticket> getTickets() { return ticketRepository.findAll(); }

    public User saveUser(User user) { return userRepository.save(user); }
    public Passenger savePassenger(Passenger p) { return passengerRepository.save(p); }
    public Order saveOrder(Order o) { return orderRepository.save(o); }
    public Ticket saveTicket(Ticket t) { return ticketRepository.save(t); }
}
