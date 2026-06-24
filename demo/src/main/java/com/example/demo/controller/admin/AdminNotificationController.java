package com.example.demo.controller.admin;

import com.example.demo.entity.Notification;
import com.example.demo.repository.NotificationRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final NotificationRepository notificationRepository;

    public AdminNotificationController(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @GetMapping
    public List<Notification> list() {
        return notificationRepository.findAllByOrderByCreateTimeDesc();
    }

    @PostMapping
    public Notification create(@RequestBody Notification notification) {
        notification.setType("SYSTEM");
        return notificationRepository.save(notification);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> delete(@PathVariable Long id) {
        notificationRepository.deleteById(id);
        return Map.of("message", "删除成功");
    }
}
