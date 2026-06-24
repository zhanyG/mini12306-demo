package com.example.demo.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Ticket 实体单元测试。
 * 覆盖车票创建和退票逻辑。
 */
class TicketTest {

    private Ticket ticket;

    @BeforeEach
    void setUp() {
        ticket = new Ticket(1L, 1L, "02车05D");
    }

    @Test
    @DisplayName("新车票状态为'有效'，自动生成票号")
    void shouldCreateValidTicket() {
        assertNotNull(ticket.getTicketNo());
        assertTrue(ticket.getTicketNo().startsWith("TKT"));
        assertEquals("有效", ticket.getStatus());
        assertEquals("02车05D", ticket.getSeatNumber());
        assertEquals(1L, ticket.getOrderId());
        assertEquals(1L, ticket.getPassengerId());
        assertNotNull(ticket.getIssueTime());
    }

    @Test
    @DisplayName("默认座位类型为'二等座'")
    void shouldDefaultToSecondClass() {
        assertEquals("二等座", ticket.getSeatType());
    }

    @Test
    @DisplayName("退票后状态变为'已退'")
    void shouldRefundTicket() {
        ticket.refund();

        assertEquals("已退", ticket.getStatus());
    }
}
