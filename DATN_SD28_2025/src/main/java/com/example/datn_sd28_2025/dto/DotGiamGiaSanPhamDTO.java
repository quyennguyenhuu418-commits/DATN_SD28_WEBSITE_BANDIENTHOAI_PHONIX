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
public class DotGiamGiaSanPhamDTO {
    private Integer id;
    private Integer idKhuyenMai;
    private Integer idChiTietSanPham;
    private String tenSanPham;
    private String maSanPham;
    private String tenHang;
    private String tenDanhMuc;
    private BigDecimal phanTramGiam; // Phần trăm ghi đè (nếu có)
    private Integer soLuongToiDa; // Số lượng tối đa áp dụng giảm giá (null = không giới hạn)
    private Integer soLuongDaBan; // Số lượng đã bán với giá giảm
    private LocalDateTime ngayTao;
    private String nguoiTao;
}


