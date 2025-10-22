package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Staff Notifications
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StaffNotificationDTO {
    private String type; // "new_customer", "customer_message", "customer_disconnect"
    private String sessionId;
    private String customerName;
    private String customerPhone;
    private String message;
    private LocalDateTime timestamp;
    private String priority; // "low", "medium", "high"
    private boolean isRead;
}


