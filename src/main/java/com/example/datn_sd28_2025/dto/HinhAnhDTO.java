package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HinhAnhDTO {
    private Integer id;
    private String urlAnh;
    private LocalDateTime ngayTao;
    private LocalDateTime ngaySua;
    private Integer trangThai;

    // Related entities
    private ChiTietSanPhamDTO chiTietSanPham;

    // Transient fields for JSON mapping
    private Integer idCtsp;
    private Integer idHang;
}
