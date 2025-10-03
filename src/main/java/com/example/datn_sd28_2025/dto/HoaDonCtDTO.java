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
public class HoaDonCtDTO {
    private Integer id;
    private Integer idHoaDon;
    private Integer idCtsp;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // Related entities
    private HoaDonDTO hoaDon;
    private ChiTietSanPhamDTO chiTietSanPham;
}
