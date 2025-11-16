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

    @Column(name = "google_id", length = 255)
    private String googleId;

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

    // Static builder method for compatibility
    public static KhachHangBuilder builder() {
        return new KhachHangBuilder();
    }

    public static class KhachHangBuilder {
        private Integer id;
        private String maKhachHang;
        private String hoTen;
        private String soDienThoai;
        private String taiKhoan;
        private String matKhau;
        private LocalDate ngaySinh;
        private String gioiTinh;
        private String email;
        private String googleId;
        private String diaChi;
        private LocalDateTime ngayTao;
        private LocalDateTime ngayCapNhat;
        private Integer trangThai;
        private String nguoiTao;
        private String nguoiCapNhat;
        private List<HoaDon> hoaDons;
        private List<KhachHangGiamGia> khachHangGiamGias;

        public KhachHangBuilder id(Integer id) {
            this.id = id;
            return this;
        }

        public KhachHangBuilder maKhachHang(String maKhachHang) {
            this.maKhachHang = maKhachHang;
            return this;
        }

        public KhachHangBuilder hoTen(String hoTen) {
            this.hoTen = hoTen;
            return this;
        }

        public KhachHangBuilder soDienThoai(String soDienThoai) {
            this.soDienThoai = soDienThoai;
            return this;
        }

        public KhachHangBuilder taiKhoan(String taiKhoan) {
            this.taiKhoan = taiKhoan;
            return this;
        }

        public KhachHangBuilder matKhau(String matKhau) {
            this.matKhau = matKhau;
            return this;
        }

        public KhachHangBuilder ngaySinh(LocalDate ngaySinh) {
            this.ngaySinh = ngaySinh;
            return this;
        }

        public KhachHangBuilder gioiTinh(String gioiTinh) {
            this.gioiTinh = gioiTinh;
            return this;
        }

        public KhachHangBuilder email(String email) {
            this.email = email;
            return this;
        }

        public KhachHangBuilder googleId(String googleId) {
            this.googleId = googleId;
            return this;
        }

        public KhachHangBuilder diaChi(String diaChi) {
            this.diaChi = diaChi;
            return this;
        }

        public KhachHangBuilder ngayTao(LocalDateTime ngayTao) {
            this.ngayTao = ngayTao;
            return this;
        }

        public KhachHangBuilder ngayCapNhat(LocalDateTime ngayCapNhat) {
            this.ngayCapNhat = ngayCapNhat;
            return this;
        }

        public KhachHangBuilder trangThai(Integer trangThai) {
            this.trangThai = trangThai;
            return this;
        }

        public KhachHangBuilder nguoiTao(String nguoiTao) {
            this.nguoiTao = nguoiTao;
            return this;
        }

        public KhachHangBuilder nguoiCapNhat(String nguoiCapNhat) {
            this.nguoiCapNhat = nguoiCapNhat;
            return this;
        }

        public KhachHangBuilder hoaDons(List<HoaDon> hoaDons) {
            this.hoaDons = hoaDons;
            return this;
        }

        public KhachHangBuilder khachHangGiamGias(List<KhachHangGiamGia> khachHangGiamGias) {
            this.khachHangGiamGias = khachHangGiamGias;
            return this;
        }

        public KhachHang build() {
            KhachHang khachHang = new KhachHang();
            khachHang.setId(this.id);
            khachHang.setMaKhachHang(this.maKhachHang);
            khachHang.setHoTen(this.hoTen);
            khachHang.setSoDienThoai(this.soDienThoai);
            khachHang.setTaiKhoan(this.taiKhoan);
            khachHang.setMatKhau(this.matKhau);
            khachHang.setNgaySinh(this.ngaySinh);
            khachHang.setGioiTinh(this.gioiTinh);
            khachHang.setEmail(this.email);
            khachHang.setGoogleId(this.googleId);
            khachHang.setDiaChi(this.diaChi);
            khachHang.setNgayTao(this.ngayTao);
            khachHang.setNgayCapNhat(this.ngayCapNhat);
            khachHang.setTrangThai(this.trangThai);
            khachHang.setNguoiTao(this.nguoiTao);
            khachHang.setNguoiCapNhat(this.nguoiCapNhat);
            khachHang.setHoaDons(this.hoaDons);
            khachHang.setKhachHangGiamGias(this.khachHangGiamGias);
            return khachHang;
        }
    }
}