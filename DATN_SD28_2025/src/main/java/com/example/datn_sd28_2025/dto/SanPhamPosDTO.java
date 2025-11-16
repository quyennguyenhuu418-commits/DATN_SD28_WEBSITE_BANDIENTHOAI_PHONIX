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
    private Double gia; // Giá hiển thị để bán (đã áp dụng đợt + sẽ áp dụng voucher sau)
    private Double giaGoc; // Giá gốc từ ChiTietSanPham
    private Double giaSauGiam; // Giá sau khi áp dụng đợt giảm (nếu có)
    private Double giamPhanTram; // % giảm được áp dụng (đợt)
    private String tenDotGiam; // Tên đợt giảm áp dụng
    private Integer soLuongTon; // Tổng số lượng từ ChiTietSanPham
    private String hinhAnh; // Hình ảnh đại diện
    private Integer hangId;
    private String tenHang;
    private Integer trangThai; // Trạng thái của CTSP (ChiTietSanPham)
    private Integer trangThaiSanPham; // Trạng thái của SP (SanPham) - để frontend validate
    
    // Danh mục (bổ sung để hiển thị đúng ở FE)
    private Integer danhMucId;
    private String tenDanhMuc;
    
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
















