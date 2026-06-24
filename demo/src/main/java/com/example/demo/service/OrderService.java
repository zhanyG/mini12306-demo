package com.example.demo.service;

import com.example.demo.entity.Bill;
import com.example.demo.entity.Order;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.Train;
import com.example.demo.repository.BillRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.TrainRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单业务层，处理购票、退票、改签、查询订单等核心流程。
 */
@Service
public class OrderService {

    private final TrainRepository trainRepository;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final BillRepository billRepository;

    public OrderService(TrainRepository trainRepository,
                        OrderRepository orderRepository,
                        TicketRepository ticketRepository,
                        BillRepository billRepository) {
        this.trainRepository = trainRepository;
        this.orderRepository = orderRepository;
        this.ticketRepository = ticketRepository;
        this.billRepository = billRepository;
    }

    /**
     * 创建订单（未支付），不扣减余票。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, Long trainId, Long passengerId) {
        Train train = trainRepository.findById(trainId)
                .orElseThrow(() -> new RuntimeException("车次不存在"));
        Order order = new Order(userId, trainId, passengerId, train.getPrice());
        return orderRepository.save(order);
    }

    /**
     * 支付确认：扣减余票 -> 出票 -> 创建账单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order confirmPayment(Long orderId, String payType, String payTradeNo) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        if (!"未支付".equals(order.getStatus())) {
            throw new RuntimeException("订单状态异常，无法支付");
        }

        Train train = trainRepository.findById(order.getTrainId())
                .orElseThrow(() -> new RuntimeException("车次不存在"));
        if (!train.bookSeat()) {
            throw new RuntimeException("余票不足");
        }
        trainRepository.save(train);

        order.pay();
        order.confirm();
        order.setPayType(payType);
        order.setPayTradeNo(payTradeNo);
        order.setPayTime(LocalDateTime.now());
        orderRepository.save(order);

        String seatNumber = generateSeatNumber(train);
        Ticket ticket = new Ticket(order.getId(), order.getPassengerId(), seatNumber);
        ticketRepository.save(ticket);

        String desc = train.getTrainNumber() + " " + train.getStartStation() + "→" + train.getEndStation();
        billRepository.save(new Bill(order.getId(), order.getUserId(), order.getPrice(), "PAYMENT", desc));

        return order;
    }

    /**
     * 模拟支付（开发调试用，无需第三方支付系统）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order simulatePay(Long orderId) {
        return confirmPayment(orderId, "CASH", "SIM" + System.currentTimeMillis());
    }

    /**
     * 原购票流程（保留兼容，直接完成下单+支付+出票）。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order buyTicket(Long userId, Long trainId, Long passengerId) {
        Order order = createOrder(userId, trainId, passengerId);
        return confirmPayment(order.getId(), "CASH", "SIM" + System.currentTimeMillis());
    }

    /**
     * 退票流程：仅已出票订单允许退票。
     */
    @Transactional(rollbackFor = Exception.class)
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

        // 创建退票退款账单
        billRepository.save(new Bill(orderId, order.getUserId(), order.getPrice(), "REFUND",
                "订单 " + order.getOrderNo()));

        return true;
    }

    /**
     * 改签流程：仅允许已出票订单改签到另一趟有余票的车次。
     * 成功后释放原车次余票、扣减新车次余票，并更新订单与车票信息。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order rescheduleTicket(Long orderId, Long newTrainId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));

        if (!"已出票".equals(order.getStatus())) {
            throw new RuntimeException("仅已出票订单允许改签");
        }
        if (order.getTrainId().equals(newTrainId)) {
            throw new RuntimeException("新车次不能与原车次相同");
        }

        Train oldTrain = trainRepository.findById(order.getTrainId())
                .orElseThrow(() -> new RuntimeException("原车次不存在"));
        Train newTrain = trainRepository.findById(newTrainId)
                .orElseThrow(() -> new RuntimeException("新车次不存在"));

        double oldPrice = order.getPrice();
        double newPrice = newTrain.getPrice();

        if (!newTrain.bookSeat()) {
            throw new RuntimeException("新车次余票不足，无法改签");
        }
        oldTrain.refundSeat();
        trainRepository.save(oldTrain);
        trainRepository.save(newTrain);

        // 改签差价账单
        String desc = newTrain.getTrainNumber() + " " + newTrain.getStartStation() + "→" + newTrain.getEndStation();
        if (newPrice > oldPrice) {
            billRepository.save(new Bill(orderId, order.getUserId(), newPrice - oldPrice, "CHANGE_UPGRADE", desc));
        } else if (newPrice < oldPrice) {
            billRepository.save(new Bill(orderId, order.getUserId(), oldPrice - newPrice, "CHANGE_REFUND", desc));
        }

        order.setTrainId(newTrainId);
        order.setPrice(newPrice);
        order.change();
        order = orderRepository.save(order);

        Ticket ticket = ticketRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("订单关联车票不存在"));
        ticket.setSeatNumber(generateSeatNumber(newTrain));
        ticket.setStatus("有效");
        ticket.setIssueTime(LocalDateTime.now());
        ticket.setTicketNo("TKT" + System.currentTimeMillis());
        ticketRepository.save(ticket);

        return order;
    }

    /**
     * 改签补差价支付：验证差价后执行改签，由 rescheduleTicket 生成差价账单。
     */
    @Transactional(rollbackFor = Exception.class)
    public Order payUpgrade(Long orderId, Long newTrainId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
        Train newTrain = trainRepository.findById(newTrainId)
                .orElseThrow(() -> new RuntimeException("新车次不存在"));
        double diff = newTrain.getPrice() - order.getPrice();
        if (diff <= 0) {
            throw new RuntimeException("新车次价格未升高，无需补差价");
        }
        // 由 rescheduleTicket 处理改签和差价账单
        return rescheduleTicket(orderId, newTrainId);
    }

    /** 查询用户的所有订单 */
    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    /** 根据 ID 查询订单 */
    public Order getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("订单不存在"));
    }

    /** 根据订单号查询并确认支付（支付宝异步通知使用） */
    @Transactional(rollbackFor = Exception.class)
    public Order confirmPaymentByOrderNo(String orderNo, String payType, String payTradeNo) {
        Order order = orderRepository.findByOrderNo(orderNo)
                .orElseThrow(() -> new RuntimeException("订单不存在: " + orderNo));
        return confirmPayment(order.getId(), payType, payTradeNo);
    }

    /**
     * 根据已售座位数动态生成座位号。
     */
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
}
