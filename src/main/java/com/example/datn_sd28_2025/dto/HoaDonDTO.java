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
public class HoaDonDTO {
    private Integer id;
    private String maHoaDon;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private BigDecimal tongTien;
    private BigDecimal tongTienSauGiam;
    private String loaiHoaDon;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayThanhToan;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
    private Integer trangThai;

    // Related entities
    private PhieuGiamGiaDTO phieuGiamGia;
    private KhachHangDTO khachHang;
    private NhanVienDTO nhanVien;

    // Transient fields for JSON mapping
    private Integer idPhieuGiamGia;
    private Integer idKhachHang;
    private Integer idNhanVien;
}
