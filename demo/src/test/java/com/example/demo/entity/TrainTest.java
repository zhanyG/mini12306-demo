package com.example.demo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Train 实体单元测试。
 * 覆盖核心业务方法：bookSeat() 和 refundSeat()。
 */
class TrainTest {

    private Train train;

    @BeforeEach
    void setUp() {
        train = new Train("G1001", "北京南", "上海虹桥",
                LocalDateTime.of(2026, 6, 4, 8, 0),
                LocalDateTime.of(2026, 6, 4, 12, 30),
                553.0);
        train.setTotalSeats(100);
        train.setAvailableSeats(100);
    }

    @Nested
    @DisplayName("预订座位 bookSeat()")
    class BookSeatTest {

        @Test
        @DisplayName("有余票时预订成功，余票减1")
        void shouldBookWhenAvailable() {
            boolean result = train.bookSeat();

            assertTrue(result);
            assertEquals(99, train.getAvailableSeats());
        }

        @Test
        @DisplayName("余票为0时预订失败，返回false")
        void shouldFailWhenSoldOut() {
            train.setAvailableSeats(0);

            boolean result = train.bookSeat();

            assertFalse(result);
            assertEquals(0, train.getAvailableSeats());
        }

        @Test
        @DisplayName("连续预订直到售罄")
        void shouldBookUntilSoldOut() {
            train.setAvailableSeats(3);

            assertTrue(train.bookSeat()); // 3→2
            assertTrue(train.bookSeat()); // 2→1
            assertTrue(train.bookSeat()); // 1→0
            assertFalse(train.bookSeat()); // 售罄
            assertEquals(0, train.getAvailableSeats());
        }
    }

    @Nested
    @DisplayName("退还座位 refundSeat()")
    class RefundSeatTest {

        @Test
        @DisplayName("退票时余票加1")
        void shouldRefundSeat() {
            train.setAvailableSeats(50);

            train.refundSeat();

            assertEquals(51, train.getAvailableSeats());
        }

        @Test
        @DisplayName("退票上限不超过总座位数")
        void shouldNotExceedTotalSeats() {
            train.setAvailableSeats(100); // 已满

            train.refundSeat();

            assertEquals(100, train.getAvailableSeats());
        }
    }

    @Test
    @DisplayName("新列车默认100个座位")
    void shouldDefaultTo100Seats() {
        Train defaultTrain = new Train("D321", "杭州东", "上海虹桥",
                LocalDateTime.now(), LocalDateTime.now().plusHours(1), 73.0);

        assertEquals(100, defaultTrain.getTotalSeats());
        assertEquals(100, defaultTrain.getAvailableSeats());
    }
}
