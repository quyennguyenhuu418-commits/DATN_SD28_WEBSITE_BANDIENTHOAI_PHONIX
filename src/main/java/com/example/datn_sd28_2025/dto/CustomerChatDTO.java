package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Customer Chat Information
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerChatDTO {
    private String sessionId;
    private String customerName;
    private String email;
    private String phoneNumber;
    private String gender;
    private String message;
    private LocalDateTime timestamp;
    private String status; // "waiting", "connected", "ended"
    private String assignedStaffId;
    private String assignedStaffName;
}


