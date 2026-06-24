package com.example.demo.repository;

import com.example.demo.entity.UserNotificationRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserNotificationReadRepository extends JpaRepository<UserNotificationRead, Long> {
    List<UserNotificationRead> findByUserId(Long userId);
    boolean existsByUserIdAndNotificationId(Long userId, Long notificationId);
}
