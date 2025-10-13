package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChiTietHoaDonDTO {
    private Integer id;
    private Integer hoaDonId;
    private Integer sanPhamId;
    private Integer soLuong;
    private Double donGia;
    private Double thanhTien;
    private String ghiChu;
    
    // Thông tin sản phẩm
    private String tenSanPham;
    private String hinhAnh;
    
    // IMEI đã chọn
    private List<String> selectedImeis;
}

