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
public class ChiTietKhuyenMaiDTO {
    private Integer id;
    private LocalDateTime ngayApDung;
    private Integer mucDoUuTien;
    private BigDecimal phanTramGiamGia;

    // Related entities
    private ChiTietSanPhamDTO chiTietSanPham;
    private KhuyenMaiDTO khuyenMai;

    // Transient fields for JSON mapping
    private Integer idCtsp;
    private Integer idKhuyenMai;
}
