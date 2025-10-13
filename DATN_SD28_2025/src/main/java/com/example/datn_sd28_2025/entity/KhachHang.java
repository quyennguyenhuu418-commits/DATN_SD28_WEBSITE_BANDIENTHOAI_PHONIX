package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "khach_hang")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHang {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khach_hang", unique = true, length = 50)
    private String maKhachHang;

    @Column(name = "ho_ten", nullable = false, length = 255, columnDefinition = "NVARCHAR(255)")
    private String hoTen;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    @Column(name = "tai_khoan", length = 100)
    private String taiKhoan;

    @Column(name = "mat_khau", length = 255)
    private String matKhau;


    @Column(name = "ngay_sinh")
    private LocalDate ngaySinh;

    @Column(name = "gioi_tinh", length = 10, columnDefinition = "NVARCHAR(10)")
    private String gioiTinh;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "dia_chi", length = 500, columnDefinition = "NVARCHAR(500)")
    private String diaChi;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "khach_hang_id")
    @JsonIgnore
    private List<HoaDon> hoaDons;




    @OneToMany(mappedBy = "khachHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<KhachHangGiamGia> khachHangGiamGias;
}
