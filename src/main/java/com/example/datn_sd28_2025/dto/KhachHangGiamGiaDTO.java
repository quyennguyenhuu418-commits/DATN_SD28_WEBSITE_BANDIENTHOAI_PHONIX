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
public class KhachHangGiamGiaDTO {
    private Integer id;
    private String nguoiSuDung;
    private LocalDateTime ngayCap;
    private Integer trangThai;

    // Related entities
    private KhachHangDTO khachHang;
    private PhieuGiamGiaDTO phieuGiamGia;

    // Transient fields for JSON mapping
    private Integer idKhachHang;
    private Integer idPhieuGiamGia;
}
