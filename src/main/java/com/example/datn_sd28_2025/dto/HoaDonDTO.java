package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
<<<<<<< HEAD
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
=======
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
>>>>>>> origin/Huan

@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
public class HoaDonDTO {
    private Integer id;
    private String maHoaDon;
    private Integer khachHangId;
    private Integer nhanVienId;
    private Integer phieuGiamGiaId;
    private Double tongTien;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private Double tongTienSauGiam;
    private LocalDateTime ngayThanhToan;
    private String loaiHoaDon;
    private Integer trangThai;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;

    // Thông tin khách hàng (nếu có) - for additional customer info
    private String soDienThoaiKhachHang;

    // Chi tiết hóa đơn
    private List<ChiTietHoaDonDTO> chiTietHoaDonList;
}
=======
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
>>>>>>> origin/Huan
