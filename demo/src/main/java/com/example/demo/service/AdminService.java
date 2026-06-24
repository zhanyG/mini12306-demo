package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.request.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 管理员业务：车次/用户/订单 CRUD、仪表盘统计 */
@Service
public class AdminService {

    private static final List<String> ORDER_STATUSES = List.of("未支付", "已支付", "已出票", "已退票");

    private final TrainRepository trainRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final PassengerRepository passengerRepository;
    private final OrderService orderService;
    private final BCryptPasswordEncoder passwordEncoder;

    public AdminService(TrainRepository trainRepository,
                        UserRepository userRepository,
                        OrderRepository orderRepository,
                        TicketRepository ticketRepository,
                        PassengerRepository passengerRepository,
                        OrderService orderService) {
        this.trainRepository = trainRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.passengerRepository = passengerRepository;
        this.orderService = orderService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("trainCount", trainRepository.count());
        stats.put("userCount", userRepository.count());
        stats.put("orderCount", orderRepository.count());
        stats.put("issuedOrderCount", orderRepository.findAll().stream()
                .filter(o -> "已出票".equals(o.getStatus())).count());
        return stats;
    }

    // ===== Train =====

    public List<Train> getAllTrains() {
        return trainRepository.findAll();
    }

    public Train createTrain(TrainRequest req) {
        Train train = new Train();
        applyTrainRequest(train, req);
        train.setAvailableSeats(req.getTotalSeats() != null ? req.getTotalSeats() : 100);
        return trainRepository.save(train);
    }

    public Train updateTrain(Long id, TrainRequest req) {
        Train train = trainRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("车次不存在"));
        int oldTotal = train.getTotalSeats() != null ? train.getTotalSeats() : 100;
        int oldAvailable = train.getAvailableSeats() != null ? train.getAvailableSeats() : oldTotal;
        int sold = oldTotal - oldAvailable;

        applyTrainRequest(train, req);
        int newTotal = req.getTotalSeats() != null ? req.getTotalSeats() : 100;
        train.setTotalSeats(newTotal);
        train.setAvailableSeats(Math.max(0, newTotal - sold));
        return trainRepository.save(train);
    }

    public void deleteTrain(Long id) {
        if (!trainRepository.existsById(id)) {
            throw new RuntimeException("车次不存在");
        }
        trainRepository.deleteById(id);
    }

    // ===== User =====

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User createUser(AdminUserRequest req) {
        if (req.getPassword() == null || req.getPassword().length() < 6) {
            throw new RuntimeException("密码长度不能少于6位");
        }
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setPhone(req.getPhone());
        user.setRealName(req.getRealName());
        user.setRole(req.getRole() != null ? req.getRole() : "USER");
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    public User updateUser(Long id, AdminUserRequest req) {
        User user = getUserById(id);
        if (req.getUsername() != null && !req.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(req.getUsername()).isPresent()) {
                throw new RuntimeException("用户名已存在");
            }
            user.setUsername(req.getUsername());
        }
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            if (req.getPassword().length() < 6) {
                throw new RuntimeException("密码长度不能少于6位");
            }
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getRealName() != null) user.setRealName(req.getRealName());
        if (req.getRole() != null) user.setRole(req.getRole());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        if ("admin".equals(user.getUsername())) {
            throw new RuntimeException("不能删除默认管理员账号");
        }
        userRepository.deleteById(id);
    }

    // ===== Order =====

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /** 代客下单：复用购票流程（扣票、出票、生成车票） */
    public Order createOrder(AdminOrderCreateRequest req) {
        User user = getUserById(req.getUserId());
        Passenger passenger = passengerRepository.findById(req.getPassengerId())
                .orElseThrow(() -> new RuntimeException("乘客不存在"));
        if (!passenger.getUserId().equals(user.getId())) {
            throw new RuntimeException("乘客不属于该用户");
        }
        return orderService.buyTicket(req.getUserId(), req.getTrainId(), req.getPassengerId());
    }

    @Transactional(rollbackFor = Exception.class)
    public Order updateOrder(Long id, AdminOrderUpdateRequest req) {
        Order order = getOrderById(id);
        String oldStatus = order.getStatus();

        if (req.getPrice() != null) {
            order.setPrice(req.getPrice());
        }

        if (req.getStatus() != null && !req.getStatus().equals(oldStatus)) {
            if (!ORDER_STATUSES.contains(req.getStatus())) {
                throw new RuntimeException("无效的订单状态");
            }
            syncOrderStatus(order, oldStatus, req.getStatus());
            order.setStatus(req.getStatus());
        }

        return orderRepository.save(order);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long id) {
        Order order = getOrderById(id);
        if ("已出票".equals(order.getStatus())) {
            releaseSeatForOrder(order);
            ticketRepository.findByOrderId(id).ifPresent(ticketRepository::delete);
        }
        orderRepository.deleteById(id);
    }

    private void syncOrderStatus(Order order, String oldStatus, String newStatus) {
        if ("已出票".equals(oldStatus) && "已退票".equals(newStatus)) {
            releaseSeatForOrder(order);
            ticketRepository.findByOrderId(order.getId()).ifPresent(t -> {
                t.refund();
                ticketRepository.save(t);
            });
            return;
        }
        if (!"已出票".equals(oldStatus) && "已出票".equals(newStatus)) {
            Train train = trainRepository.findById(order.getTrainId())
                    .orElseThrow(() -> new RuntimeException("车次不存在"));
            if (!train.bookSeat()) {
                throw new RuntimeException("余票不足，无法设为已出票");
            }
            trainRepository.save(train);
            if (ticketRepository.findByOrderId(order.getId()).isEmpty()) {
                String seatNumber = generateSeatNumber(train);
                ticketRepository.save(new Ticket(order.getId(), order.getPassengerId(), seatNumber));
            }
        }
    }

    private void releaseSeatForOrder(Order order) {
        Train train = trainRepository.findById(order.getTrainId()).orElse(null);
        if (train != null) {
            train.refundSeat();
            trainRepository.save(train);
        }
    }

    private String generateSeatNumber(Train train) {
        int seatIndex = train.getTotalSeats() - train.getAvailableSeats();
        int seatsPerCar = 50;
        int car = (seatIndex / seatsPerCar) + 1;
        int seatInCar = seatIndex % seatsPerCar;
        int row = (seatInCar / 5) + 1;
        char[] seatLetters = {'A', 'B', 'C', 'D', 'F'};
        char seatLetter = seatLetters[seatInCar % 5];
        return String.format("%02d车%02d%c", car, row, seatLetter);
    }

    private void applyTrainRequest(Train train, TrainRequest req) {
        train.setTrainNumber(req.getTrainNumber());
        train.setStartStation(req.getStartStation());
        train.setEndStation(req.getEndStation());
        train.setDepartureTime(req.getDepartureTime());
        train.setArrivalTime(req.getArrivalTime());
        train.setPrice(req.getPrice());
        if (req.getTotalSeats() != null) {
            train.setTotalSeats(req.getTotalSeats());
        }
    }
}
