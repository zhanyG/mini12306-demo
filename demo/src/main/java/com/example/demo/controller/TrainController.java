package com.example.demo.controller;

import com.example.demo.entity.Train;
import com.example.demo.service.DataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 车次相关 API：查询所有车次、按起止站搜索。
 */
@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final DataService dataService;

    public TrainController(DataService dataService) {
        this.dataService = dataService;
    }

    /** 获取所有车次列表 */
    @GetMapping
    public List<Train> getAllTrains() {
        return dataService.getTrains();
    }

    /**
     * 按起止站模糊搜索车次（数据库层查询，而非内存过滤）。
     * @param start 始发站（可选）
     * @param end   终点站（可选）
     * @param date  出发日期，格式 yyyy-MM-dd（可选）
     */
    @GetMapping("/search")
    public List<Train> searchTrains(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(required = false) String date) {
        return dataService.searchTrains(start, end, date);
    }

    /** 根据 ID 查询车次详情 */
    @GetMapping("/{id}")
    public Train getTrain(@PathVariable Long id) {
        Train train = dataService.getTrainById(id);
        if (train == null) throw new RuntimeException("车次不存在");
        return train;
    }
}
