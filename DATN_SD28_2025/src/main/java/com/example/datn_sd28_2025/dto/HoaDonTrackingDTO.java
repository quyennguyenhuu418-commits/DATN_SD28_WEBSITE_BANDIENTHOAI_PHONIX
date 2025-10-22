package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonTrackingDTO {
    private Integer id;
    private String maHoaDon;
    private String loaiHoaDon;
    private Integer trangThai;
    private String phieuGiamGia;
    private LocalDateTime ngayDat;
    
    // Thông tin nhân viên
    private Integer nhanVienId;
    private String tenNhanVien;
    
    // Thông tin khách hàng
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private String email;
    private String ghiChu;
    
    // Thông tin địa chỉ và giao hàng
    private String tinhThanh;
    private String quanHuyen;
    private String phuongThucGiaoHang;
    private String phuongThucThanhToan;
    private Double phiVanChuyen;
    
    // Chi tiết sản phẩm
    private List<SanPhamTrackingDTO> danhSachSanPham;
    
    // Tổng kết đơn hàng
    private Double tongTienHang;
    private Double giamGia;
    private Double thanhTien;
    
    // Thông tin tracking
    private List<TrangThaiTrackingDTO> lichSuTrangThai;
}
