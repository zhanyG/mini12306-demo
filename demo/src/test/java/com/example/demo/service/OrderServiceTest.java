package com.example.demo.service;

import com.example.demo.entity.Bill;
import com.example.demo.entity.Order;
import com.example.demo.entity.Ticket;
import com.example.demo.entity.Train;
import com.example.demo.repository.BillRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.TicketRepository;
import com.example.demo.repository.TrainRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * OrderService 单元测试。
 * 使用 Mockito 模拟 Repository 层，聚焦业务逻辑验证。
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private TrainRepository trainRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private BillRepository billRepository;

    @InjectMocks
    private OrderService orderService;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<Ticket> ticketCaptor;

    private Train train;
    private Order order;
    private Ticket ticket;

    @BeforeEach
    void setUp() {
        train = new Train("G1001", "北京南", "上海虹桥",
                LocalDateTime.of(2026, 6, 4, 8, 0),
                LocalDateTime.of(2026, 6, 4, 12, 30), 553.0);
        train.setId(1L);
        train.setTotalSeats(100);
        train.setAvailableSeats(50);

        order = new Order(1L, 1L, 1L, 553.0);
        order.setId(100L);
        order.confirm(); // 已出票

        ticket = new Ticket(100L, 1L, "01车01A");
    }

    // ==================== buyTicket ====================

    @Nested
    @DisplayName("购票 buyTicket()")
    class BuyTicketTest {

        /**
         * 每个购票测试前，设置 createOrder + confirmPayment 能正常走通所需的通用 mock。
         * 注意：trainRepository.findById 在每个测试中单独设置（不同测试需要不同余票数）。
         */
        @BeforeEach
        void setUp() {
            // 使用 lenient() 允许某些测试不触发全部 mock（例如异常路径提前返回）
            // createOrder: 保存时设置 ID，模拟 JPA 自动生成主键
            lenient().when(orderRepository.save(any(Order.class))).thenAnswer(i -> {
                Order o = i.getArgument(0);
                if (o.getId() == null) o.setId(100L);
                return o;
            });
            // confirmPayment: 按 ID 查找订单（返回"未支付"的新订单）
            Order unpaid = new Order(1L, 1L, 1L, 553.0);
            unpaid.setId(100L);
            lenient().when(orderRepository.findById(100L)).thenReturn(Optional.of(unpaid));
            // confirmPayment 中出票和账单的 mock
            lenient().when(ticketRepository.save(any(Ticket.class))).thenAnswer(i -> i.getArgument(0));
            lenient().when(billRepository.save(any(Bill.class))).thenReturn(null);
        }

        @Test
        @DisplayName("正常购票成功，余票扣减，订单支付出票")
        void shouldBuyTicketSuccessfully() {
            when(trainRepository.findById(1L)).thenReturn(Optional.of(train));

            Order result = orderService.buyTicket(1L, 1L, 1L);

            // 验证余票扣减
            assertEquals(49, train.getAvailableSeats());
            // 验证订单状态
            assertEquals("已出票", result.getStatus());
            assertTrue(result.getOrderNo().startsWith("ORD"));
            // 验证车票被保存
            verify(ticketRepository, times(1)).save(ticketCaptor.capture());
            Ticket savedTicket = ticketCaptor.getValue();
            assertEquals("有效", savedTicket.getStatus());
            assertEquals(1L, savedTicket.getPassengerId());
            assertNotNull(savedTicket.getSeatNumber());
        }

        @Test
        @DisplayName("车次不存在时抛出异常")
        void shouldThrowWhenTrainNotFound() {
            when(trainRepository.findById(999L)).thenReturn(Optional.empty());

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orderService.buyTicket(1L, 999L, 1L));
            assertTrue(ex.getMessage().contains("车次不存在"));
        }

        @Test
        @DisplayName("余票不足时抛出异常")
        void shouldThrowWhenNoSeats() {
            train.setAvailableSeats(0);
            when(trainRepository.findById(1L)).thenReturn(Optional.of(train));

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> orderService.buyTicket(1L, 1L, 1L));
            assertTrue(ex.getMessage().contains("余票不足"));
        }
    }

    // ==================== cancelTicket ====================

    @Nested
    @DisplayName("退票 cancelTicket()")
    class CancelTicketTest {

        @Test
        @DisplayName("已出票订单可正常退票")
        void shouldCancelConfirmedOrder() {
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(trainRepository.findById(1L)).thenReturn(Optional.of(train));
            when(ticketRepository.findByOrderId(100L)).thenReturn(Optional.of(ticket));

            boolean result = orderService.cancelTicket(100L);

            assertTrue(result);
            assertEquals("已退票", order.getStatus());
            assertEquals(51, train.getAvailableSeats()); // 余票释放 +1
            assertEquals("已退", ticket.getStatus()); // 车票已退
        }

        @Test
        @DisplayName("非已出票状态不能退票")
        void shouldNotCancelNonConfirmedOrder() {
            order.setStatus("未支付");
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            boolean result = orderService.cancelTicket(100L);

            assertFalse(result);
            verify(trainRepository, never()).save(any());
        }

        @Test
        @DisplayName("不存在的订单返回false")
        void shouldReturnFalseWhenOrderNotFound() {
            when(orderRepository.findById(999L)).thenReturn(Optional.empty());

            boolean result = orderService.cancelTicket(999L);

            assertFalse(result);
        }

        @Test
        @DisplayName("退票时即使车次不存在也应完成退票")
        void shouldCancelEvenWhenTrainMissing() {
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));
            when(trainRepository.findById(1L)).thenReturn(Optional.empty());
            when(ticketRepository.findByOrderId(100L)).thenReturn(Optional.of(ticket));

            boolean result = orderService.cancelTicket(100L);

            assertTrue(result);
            assertEquals("已退票", order.getStatus());
        }

        @Test
        @DisplayName("一个订单退多次幂等——第二次返回false")
        void shouldBeIdempotent() {
            order.setStatus("已退票");
            when(orderRepository.findById(100L)).thenReturn(Optional.of(order));

            boolean result = orderService.cancelTicket(100L);

            assertFalse(result);
        }
    }

    // ==================== getUserOrders ====================

    @Nested
    @DisplayName("查询订单 getUserOrders()")
    class GetUserOrdersTest {

        @Test
        @DisplayName("按用户ID查询订单列表")
        void shouldReturnUserOrders() {
            when(orderRepository.findByUserId(1L)).thenReturn(List.of(order));

            List<Order> orders = orderService.getUserOrders(1L);

            assertEquals(1, orders.size());
            assertEquals(100L, orders.get(0).getId());
        }

        @Test
        @DisplayName("无订单时返回空列表")
        void shouldReturnEmptyList() {
            when(orderRepository.findByUserId(99L)).thenReturn(List.of());

            List<Order> orders = orderService.getUserOrders(99L);

            assertTrue(orders.isEmpty());
        }
    }
}
