package com.example.demo.controller;

import com.example.demo.entity.Passenger;
import com.example.demo.service.DataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 常用乘客管理 API：增删改查。
 * 每个乘客归属于一个用户（userId），用户登录后可管理自己的常用乘客列表。
 */
@RestController
@RequestMapping("/api/passengers")
public class PassengerController {

    private final DataService dataService;

    public PassengerController(DataService dataService) {
        this.dataService = dataService;
    }

    /** 添加常用乘客 */
    @PostMapping
    public Passenger addPassenger(@RequestBody Passenger passenger) {
        if (passenger.getUserId() == null || passenger.getName() == null || passenger.getName().isBlank()) {
            throw new RuntimeException("用户ID和乘客姓名不能为空");
        }
        return dataService.savePassenger(passenger);
    }

    /** 查询某用户的所有常用乘客 */
    @GetMapping("/user/{userId}")
    public List<Passenger> getUserPassengers(@PathVariable Long userId) {
        return dataService.getPassengersByUserId(userId);
    }

    /** 根据 ID 查询乘客 */
    @GetMapping("/{id}")
    public Passenger getPassenger(@PathVariable Long id) {
        return dataService.getPassengerById(id);
    }

    /** 更新乘客信息 */
    @PutMapping("/{id}")
    public Passenger updatePassenger(@PathVariable Long id, @RequestBody Passenger updated) {
        Passenger p = dataService.getPassengerById(id);
        if (p == null) {
            throw new RuntimeException("乘客不存在");
        }
        if (updated.getName() != null) p.setName(updated.getName());
        if (updated.getIdCard() != null) p.setIdCard(updated.getIdCard());
        if (updated.getPhone() != null) p.setPhone(updated.getPhone());
        return dataService.savePassenger(p);
    }

    /** 删除乘客 */
    @DeleteMapping("/{id}")
    public String deletePassenger(@PathVariable Long id) {
        Passenger p = dataService.getPassengerById(id);
        if (p == null) {
            throw new RuntimeException("乘客不存在");
        }
        dataService.deletePassenger(id);
        return "删除成功";
    }
}
