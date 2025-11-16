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
    private LocalDateTime ngayCapNhat;

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
    private Double tongTien;

    // Thông tin tracking
    private List<TrangThaiTrackingDTO> lichSuTrangThai;

    // Additional getter methods for compatibility
    public String getTenTrangThai() {
        if (lichSuTrangThai != null && !lichSuTrangThai.isEmpty()) {
            return lichSuTrangThai.get(lichSuTrangThai.size() - 1).getTenTrangThai();
        }
        return getTrangThaiText();
    }

    public String getTrangThaiText() {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case 0: return "Chờ xác nhận";
            case 1: return "Đang giao hàng";
            case 2: return "Đã giao hàng";
            case 3: return "Đã thanh toán";
            case 4: return "Hoàn hàng";
            case 5: return "Hủy";
            default: return "Không xác định";
        }
    }
}
