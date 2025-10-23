package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hoa_don")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hoa_don", unique = true)
    private String maHoaDon;

    @Column(name = "id_khach_hang")
    private Integer khachHangId;

    @Column(name = "id_nhan_vien")
    private Integer nhanVienId;

    @Column(name = "id_phieu_giam_gia")
    private Integer phieuGiamGiaId;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "email")
    private String email;

    @Column(name = "tinh_thanh")
    private String tinhThanh;

    @Column(name = "quan_huyen")
    private String quanHuyen;

    @Column(name = "phuong_thuc_giao_hang")
    private String phuongThucGiaoHang;

    @Column(name = "phuong_thuc_thanh_toan")
    private String phuongThucThanhToan;

    @Column(name = "phi_van_chuyen")
    private Double phiVanChuyen;

    @Column(name = "tong_tien_sau_giam")
    private Double tongTienSauGiam;

    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "loai_hoa_don")
    private String loaiHoaDon; // BAN_THUONG (Bán tại quầy), BAN_ONLINE

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietThanhToan> chiTietThanhToanList;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Chờ xác nhận, 1: Đang giao hàng, 2: Đã giao hàng, 3: Đã thanh toán, 4: Hoàn hàng, 5: Hủy

    // @Column(name = "dia_chi_giao_hang", nullable = true)
    // private String diaChiGiaoHang;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao")
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat")
    private String nguoiCapNhat;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietHoaDon> chiTietHoaDonList;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (maHoaDon == null) {
            maHoaDon = generateMaHoaDon();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    private String generateMaHoaDon() {
        return "HD" + System.currentTimeMillis();
    }
}