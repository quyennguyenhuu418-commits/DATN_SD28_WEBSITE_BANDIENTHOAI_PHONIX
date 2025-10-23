package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonCtDTO {
    private Integer id;
    private Integer idHoaDon;
    private Integer idCtsp;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
    // Note: ngay_tao and ngay_cap_nhat columns don't exist in the actual database
    // private LocalDateTime ngayTao;
    // private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // Related entities
    private HoaDonDTO hoaDon;
    private ChiTietSanPhamDTO chiTietSanPham;
    private List<ImeiDaBanDTO> imeiDaBans;
}