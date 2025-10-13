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
public class SanPhamDTO {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String thietKe;
    private String kichThuoc;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
    private Integer trangThai;

    // Flattened fields for display
    private String tenDanhMuc;
    private String tenHang;
    private String tenManHinh;
    private String tenCameraTruoc;
    private String tenCameraSau;
    private String tenChip;
    private String tenGpu;
    private String tenSim;
    private String tenHeDieuHanh;
    private String tenCpu;
    private String tenPin;

    // Optional aggregated fields
    private Integer tongSoLuong;
    private Integer tongImei;
}


