package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoucherUsageRequest {
    private Integer customerId;
    private String voucherCode;
    private Double orderAmount; // Số tiền đơn hàng để validate
}
















