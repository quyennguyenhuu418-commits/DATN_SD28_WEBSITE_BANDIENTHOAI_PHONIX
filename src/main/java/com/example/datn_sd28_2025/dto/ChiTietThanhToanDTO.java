package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietThanhToanDTO {
    private Integer id;
    private BigDecimal soTien;
    private String maGiaoDich;
    private LocalDateTime ngayThanhToan;
    private Integer trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;

    // Related entities
    private HoaDonDTO donHang;
    private PhuongThucThanhToanDTO phuongThucThanhToan;

    // Transient fields for JSON mapping
    private Integer idDonHang;
    private Integer idPhuongThucThanhToan;
}
