package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OnlineOrderRequest {
    // Customer information
    private String tenKhachHang;
    private String soDienThoai;
    private String email;
    private String diaChi;
    private String tinhThanh;
    private String quanHuyen;
    
    // Order details
    private String phuongThucGiaoHang;
    private String phuongThucThanhToan;
    private String ghiChu;
    private String loaiHoaDon; // Luôn là "ONLINE" cho đơn hàng online
    
    // Pricing
    private BigDecimal tongTien;
    private BigDecimal phiVanChuyen;
    private BigDecimal tongTienSauGiam;
    private Integer phieuGiamGiaId;
    
    // Items
    private List<ChiTietDonHangRequest> chiTietDonHang;
    
    // Status
    private String trangThai;
    
    // Timestamps
    private String ngayTao;
    private String ngayCapNhat;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChiTietDonHangRequest {
        private Integer chiTietSanPhamId;
        private Integer soLuong;
        private BigDecimal gia;
        private BigDecimal thanhTien;
    }
}







