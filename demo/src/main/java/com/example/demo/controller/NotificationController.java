package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.UserNotificationRead;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserNotificationReadRepository;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationRepository notificationRepository;
    private final UserNotificationReadRepository readRepository;

    public NotificationController(NotificationRepository notificationRepository,
                                  UserNotificationReadRepository readRepository) {
        this.notificationRepository = notificationRepository;
        this.readRepository = readRepository;
    }

    @GetMapping
    public Map<String, Object> list(@RequestParam(required = false) Long userId) {
        List<Notification> list = notificationRepository.findAllByOrderByCreateTimeDesc();
        List<Long> readIds = List.of();
        if (userId != null) {
            readIds = readRepository.findByUserId(userId).stream()
                    .map(UserNotificationRead::getNotificationId)
                    .collect(Collectors.toList());
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("readIds", readIds);
        return result;
    }

    @GetMapping("/{id}")
    public Notification get(@PathVariable Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("通知不存在"));
    }

    @PostMapping("/{id}/read")
    public Map<String, Object> markRead(@PathVariable Long id, @RequestParam Long userId) {
        if (!readRepository.existsByUserIdAndNotificationId(userId, id)) {
            UserNotificationRead read = new UserNotificationRead();
            read.setUserId(userId);
            read.setNotificationId(id);
            readRepository.save(read);
        }
        long total = notificationRepository.count();
        long readCount = readRepository.findByUserId(userId).size();
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", (int) (total - readCount));
        return result;
    }

    @GetMapping("/unread-count")
    public Map<String, Object> unreadCount(@RequestParam Long userId) {
        long total = notificationRepository.count();
        long readCount = readRepository.findByUserId(userId).size();
        long unread = total - readCount;
        Map<String, Object> result = new HashMap<>();
        result.put("unreadCount", (int) Math.max(0, unread));
        return result;
    }
}
