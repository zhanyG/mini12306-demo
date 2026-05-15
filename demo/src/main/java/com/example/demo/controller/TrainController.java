package com.example.demo.controller;

import com.example.demo.entity.Train;
import com.example.demo.service.DataService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trains")
public class TrainController {

    private final DataService dataService;

    public TrainController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping
    public List<Train> getAllTrains() {
        return dataService.getTrains();
    }

    @GetMapping("/search")
    public List<Train> searchTrains(
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end) {
        return dataService.getTrains().stream()
                .filter(t -> (start == null || t.getStartStation().contains(start)) &&
                        (end == null || t.getEndStation().contains(end)))
                .toList();
    }
}