package com.example.demo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Order 实体单元测试。
 * 覆盖订单状态转换：未支付 → 已支付 → 已出票 → 已退票。
 */
class OrderTest {

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order(1L, 1L, 1L, 553.0);
    }

    @Test
    @DisplayName("新订单状态为'未支付'且自动生成订单号")
    void shouldCreateUnpaidOrder() {
        assertNotNull(order.getOrderNo());
        assertTrue(order.getOrderNo().startsWith("ORD"));
        assertEquals("未支付", order.getStatus());
        assertEquals(553.0, order.getPrice());
        assertNotNull(order.getCreateTime());
    }

    @Nested
    @DisplayName("状态转换")
    class StateTransitionTest {

        @Test
        @DisplayName("支付后状态变为'已支付'")
        void shouldPay() {
            order.pay();
            assertEquals("已支付", order.getStatus());
        }

        @Test
        @DisplayName("出票后状态变为'已出票'")
        void shouldConfirm() {
            order.confirm();
            assertEquals("已出票", order.getStatus());
        }

        @Test
        @DisplayName("取消后状态变为'已退票'")
        void shouldCancel() {
            order.cancel();
            assertEquals("已退票", order.getStatus());
        }

        @Test
        @DisplayName("完整的购票生命周期：未支付→已支付→已出票")
        void shouldTransitionThroughStates() {
            assertEquals("未支付", order.getStatus());

            order.pay();
            assertEquals("已支付", order.getStatus());

            order.confirm();
            assertEquals("已出票", order.getStatus());
        }

        @Test
        @DisplayName("已出票可退票：已出票→已退票")
        void shouldCancelAfterConfirm() {
            order.pay();
            order.confirm();
            order.cancel();

            assertEquals("已退票", order.getStatus());
        }
    }
}
