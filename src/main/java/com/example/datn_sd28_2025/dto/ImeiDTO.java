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
public class ImeiDTO {
    private Integer id;
    private String imei;
    // Note: ngay_tao and ngay_cap_nhat columns don't exist in the actual database
    // private LocalDateTime ngayTao;
    // private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // Related entities
    // private ChiTietSanPhamDTO chiTietSanPham;

    // Transient fields for JSON mapping
    private Integer idCtsp;
}
