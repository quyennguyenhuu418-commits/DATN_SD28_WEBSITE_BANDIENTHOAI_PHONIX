package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPreviewResponse {
    private boolean valid;
    private String message;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal total;
    private PhieuGiamGiaDTO voucher; // snapshot voucher dùng để hiển thị nếu hợp lệ
}







