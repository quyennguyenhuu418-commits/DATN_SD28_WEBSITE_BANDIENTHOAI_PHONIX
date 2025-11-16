package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuBaoHanhDTO {
    private Integer id;
    private String maPhieu;

    // Thông tin khách hàng
    private Integer khachHangId;
    private String tenKhachHang;
    private String soDienThoai;

    // Thông tin sản phẩm
    private Integer sanPhamId;
    private Integer chiTietSanPhamId;
    private Integer hoaDonId;
    private String tenSanPham;
    private String imeiSerial;

    // Tình trạng tiếp nhận
    private String moTaLoiKhachHang;
    private String moTaLoiNhanVien;
    private String tinhTrangVatLy;
    private String phuKienDiKem;

    // Đánh giá điều kiện bảo hành
    private Boolean duDieuKienBaoHanh;
    private String lyDoKhongDuDieuKien;

    // Hướng xử lý
    private String huongXuLy; // "SUA_TAI_CUA_HANG", "GUI_TTBH_HANG"

    // Kết quả xử lý
    private String noiDungSuaChua;
    private String ghiChuKyThuatVien;
    private BigDecimal chiPhiSuaChua;
    private BigDecimal khachDaThanhToan;

    // Thông tin TTBH hãng
    private String ttbhHang;
    private String maBaoHanhHang;

    // Nhân viên xử lý
    private Integer nhanVienTiepNhanId;
    private Integer nhanVienKyThuatId;
    private Integer nhanVienTraMayId;
    private String tenNhanVienTiepNhan;
    private String tenNhanVienKyThuat;
    private String tenNhanVienTraMay;

    // Thời gian
    private LocalDate ngayNhan;
    private LocalDate ngayHenTraDuKien;
    private LocalDate ngayTraThucTe;

    // Trạng thái
    private Integer trangThai;
    private String trangThaiText; // Trạng thái dạng text để hiển thị

    // Timestamps
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;

    // Lịch sử xử lý
    private List<LichSuXuLyBaoHanhDTO> lichSuXuLyBaoHanhList;

    // Transient fields for display
    private String tenKhachHangDisplay;
    private String tenSanPhamDisplay;
}

