package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.Notification;
import com.example.datn_sd28_2025.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    // Lấy danh sách thông báo chưa đọc
    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications() {
        try {
            List<Notification> notifications = notificationService.getUnreadNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error getting unread notifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Lấy danh sách thông báo gần đây
    @GetMapping("/recent")
    public ResponseEntity<List<Notification>> getRecentNotifications() {
        try {
            List<Notification> notifications = notificationService.getRecentNotifications();
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error getting recent notifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Đếm số thông báo chưa đọc
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        try {
            Long count = notificationService.getUnreadCount();
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            System.err.println("Error getting unread count: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Đánh dấu tất cả thông báo là đã đọc
    @PutMapping("/mark-all-read")
    public ResponseEntity<Map<String, Object>> markAllAsRead() {
        try {
            int updatedCount = notificationService.markAllAsRead();
            return ResponseEntity.ok(Map.of(
                "message", "Đã đánh dấu tất cả thông báo là đã đọc",
                "updatedCount", updatedCount
            ));
        } catch (Exception e) {
            System.err.println("Error marking all as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Đánh dấu thông báo cụ thể là đã đọc
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id) {
        try {
            int updatedCount = notificationService.markAsRead(id);
            if (updatedCount > 0) {
                return ResponseEntity.ok(Map.of(
                    "message", "Đã đánh dấu thông báo là đã đọc",
                    "updatedCount", updatedCount
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "message", "Thông báo không tồn tại hoặc đã được đánh dấu đã đọc",
                    "updatedCount", updatedCount
                ));
            }
        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Lấy thông báo theo loại
    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> getNotificationsByType(@PathVariable String type) {
        try {
            List<Notification> notifications = notificationService.getNotificationsByType(type);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            System.err.println("Error getting notifications by type: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Lấy thống kê thông báo
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getNotificationStats() {
        try {
            Map<String, Object> stats = notificationService.getNotificationStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("Error getting notification stats: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Xóa thông báo cũ
    @DeleteMapping("/cleanup")
    public ResponseEntity<Map<String, String>> cleanupOldNotifications() {
        try {
            notificationService.deleteOldNotifications();
            return ResponseEntity.ok(Map.of("message", "Đã xóa thông báo cũ"));
        } catch (Exception e) {
            System.err.println("Error cleaning up old notifications: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
