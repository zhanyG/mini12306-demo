package com.example.demo.controller.admin;

import com.example.demo.entity.Train;
import com.example.demo.request.TrainRequest;
import com.example.demo.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/trains")
public class AdminTrainController {

    private final AdminService adminService;

    public AdminTrainController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping
    public List<Train> list() {
        return adminService.getAllTrains();
    }

    @PostMapping
    public Train create(@Valid @RequestBody TrainRequest request) {
        return adminService.createTrain(request);
    }

    @PutMapping("/{id}")
    public Train update(@PathVariable Long id, @Valid @RequestBody TrainRequest request) {
        return adminService.updateTrain(id, request);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        adminService.deleteTrain(id);
        return Map.of("message", "删除成功");
    }
}
