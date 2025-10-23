package com.example.datn_sd28_2025.entity;

<<<<<<< HEAD
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

=======
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
>>>>>>> origin/Huan
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "hoa_don")
@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
public class HoaDon {

=======
@Builder
public class HoaDon {
>>>>>>> origin/Huan
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

<<<<<<< HEAD
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
=======
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    @JsonIgnore
    private PhieuGiamGia phieuGiamGia;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    @JsonIgnore
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    @JsonIgnore
    private KhachHang khachHang;


    @Column(name = "ma_hoa_don", unique = true, length = 50)
    private String maHoaDon;

    @Column(name = "ten_khach_hang", length = 255)
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", length = 20)
    private String soDienThoai;

    @Column(name = "dia_chi", length = 500)
    private String diaChi;

    @Column(name = "tong_tien", precision = 18, scale = 2)
    private BigDecimal tongTien;

    @Column(name = "tong_tien_sau_giam", precision = 18, scale = 2)
    private BigDecimal tongTienSauGiam;

    @Column(name = "loai_hoa_don", length = 100)
    private String loaiHoaDon;

    @Column(name = "ghi_chu", length = 500)
>>>>>>> origin/Huan
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

<<<<<<< HEAD
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
=======
    @Column(name = "ngay_thanh_toan")
    private LocalDateTime ngayThanhToan;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "hoaDon", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonCt> hoaDonCts;

    @OneToMany(mappedBy = "donHang", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietThanhToan> chiTietThanhToans;

    // Transient fields for JSON mapping
    @Transient
    @JsonProperty("idPhieuGiamGia")
    private Integer idPhieuGiamGia;

    @Transient
    @JsonProperty("idNhanVien")
    private Integer idNhanVien;

    @Transient
    @JsonProperty("idKhachHang")
    private Integer idKhachHang;

}
>>>>>>> origin/Huan
