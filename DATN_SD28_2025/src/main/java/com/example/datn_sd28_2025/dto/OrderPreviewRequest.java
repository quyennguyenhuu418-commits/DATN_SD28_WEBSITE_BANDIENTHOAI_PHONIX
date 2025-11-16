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
public class OrderPreviewRequest {
    private BigDecimal subtotal; // Tổng tiền hàng trước giảm giá
    private String voucherCode;  // Mã voucher (có thể null)
    private Integer customerId;  // ID khách hàng (có thể null)
    private String paymentMethod; // cash|vnpay|combined (tùy điều kiện áp dụng)
}







