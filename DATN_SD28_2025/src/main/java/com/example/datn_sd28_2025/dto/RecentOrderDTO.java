package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentOrderDTO {
    private Long id;
    private String orderCode;
    private String customerName;
    private String customerPhone;
    private BigDecimal totalAmount;
    private Integer status;
    private String statusText;
    private LocalDateTime createdAt;
    private String paymentMethod;
    private String orderType;
}


