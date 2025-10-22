package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamPosDTO {
    private Integer id;
    private String tenSanPham;
    private Double gia; // Giá bán từ ChiTietSanPham
    private Integer soLuongTon; // Tổng số lượng từ ChiTietSanPham
    private String hinhAnh; // Hình ảnh đại diện
    private Integer hangId;
    private String tenHang;
    private Integer trangThai;
    
    // Chi tiết sản phẩm ID để tham chiếu khi bán
    private Integer chiTietSanPhamId;
    private String maCtsp;
    private Integer romId;
    private Integer ramId;
    private Integer mauSacId;
    private String tenRom;
    private String tenRam;
    private String tenMauSac;
}
















