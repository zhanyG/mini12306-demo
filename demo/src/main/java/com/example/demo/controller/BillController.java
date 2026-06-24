package com.example.demo.controller;

import com.example.demo.entity.Bill;
import com.example.demo.repository.BillRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账单相关 API：查询用户账单。
 */
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillRepository billRepository;

    public BillController(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    /** 查询用户的所有账单（按时间倒序） */
    @GetMapping("/user/{userId}")
    public List<Bill> getUserBills(@PathVariable Long userId) {
        List<Bill> bills = billRepository.findByUserIdOrderByCreateTimeDesc(userId);
        return bills;
    }

    /** 查询订单关联的账单 */
    @GetMapping("/order/{orderId}")
    public List<Bill> getOrderBills(@PathVariable Long orderId) {
        return billRepository.findByOrderId(orderId);
    }
}
