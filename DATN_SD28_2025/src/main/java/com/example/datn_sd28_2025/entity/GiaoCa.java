package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Entity
@Table(name = "giao_ca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiaoCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã giao ca không được để trống")
    @Size(max = 50, message = "Mã giao ca không được quá 50 ký tự")
    @Column(name = "ma_giao_ca", unique = true, length = 50)
    private String maGiaoCa;

    @NotNull(message = "Phân ca không được để trống")
    @Column(name = "phan_ca_id", nullable = false)
    private Integer phanCaId;

    @NotNull(message = "Nhân viên giao không được để trống")
    @Column(name = "nhan_vien_giao_id", nullable = false)
    private Integer nhanVienGiaoId;

    @Column(name = "nhan_vien_nhan_id")
    private Integer nhanVienNhanId;

    @NotNull(message = "Ngày giao ca không được để trống")
    @Column(name = "ngay_giao_ca", nullable = false)
    private LocalDateTime ngayGiaoCa;

    // Thông tin tiền mặt
    @Column(name = "so_tien_dau_ca", precision = 18, scale = 2)
    private BigDecimal soTienDauCa;

    @Column(name = "so_tien_cuoi_ca", precision = 18, scale = 2)
    private BigDecimal soTienCuoiCa;

    @Column(name = "so_tien_thu_them", precision = 18, scale = 2)
    private BigDecimal soTienThuThem;

    @Column(name = "so_tien_chi_ra", precision = 18, scale = 2)
    private BigDecimal soTienChiRa;

    @Column(name = "chenh_lech", precision = 18, scale = 2)
    private BigDecimal chenhLech;

    // Thông tin doanh thu ca
    @Column(name = "tong_doanh_thu", precision = 18, scale = 2)
    private BigDecimal tongDoanhThu;

    @Column(name = "so_don_hang")
    private Integer soDonHang;

    @Column(name = "so_don_hang_thanh_toan_tien_mat")
    private Integer soDonHangThanhToanTienMat;

    @Column(name = "so_don_hang_thanh_toan_chuyen_khoan")
    private Integer soDonHangThanhToanChuyenKhoan;

    // Trạng thái giao ca
    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Chờ xác nhận, 1: Đã xác nhận, 2: Đã hủy

    @Column(name = "thoi_gian_xac_nhan")
    private LocalDateTime thoiGianXacNhan;

    // Ghi chú và báo cáo
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    @Column(name = "ghi_chu", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String ghiChu;

    @Size(max = 2000, message = "Báo cáo công việc không được quá 2000 ký tự")
    @Column(name = "bao_cao_cong_viec", length = 2000, columnDefinition = "NVARCHAR(2000)")
    private String baoCaoCongViec;

    @Size(max = 1000, message = "Sự cố bất thường không được quá 1000 ký tự")
    @Column(name = "su_co_bat_thuong", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String suCoBatThuong;

    @Size(max = 1000, message = "Công việc tồn đọng không được quá 1000 ký tự")
    @Column(name = "cong_viec_ton_dong", length = 1000, columnDefinition = "NVARCHAR(1000)")
    private String congViecTonDong;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Size(max = 255, message = "Người tạo không được quá 255 ký tự")
    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Size(max = 255, message = "Người cập nhật không được quá 255 ký tự")
    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phan_ca_id", insertable = false, updatable = false)
    private PhanCa phanCa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_giao_id", insertable = false, updatable = false)
    private NhanVien nhanVienGiao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nhan_vien_nhan_id", insertable = false, updatable = false)
    private NhanVien nhanVienNhan;

    @OneToMany(mappedBy = "giaoCa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ChiTietGiaoCa> chiTietGiaoCaList;

    @OneToMany(mappedBy = "giaoCa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LichSuGiaoCa> lichSuGiaoCaList;

    @PrePersist
    protected void onCreate() {
        // Sử dụng múi giờ Việt Nam (Asia/Ho_Chi_Minh)
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ngayTao = LocalDateTime.now(vietnamZone);
        ngayCapNhat = LocalDateTime.now(vietnamZone);
        if (maGiaoCa == null) {
            maGiaoCa = "GC" + System.currentTimeMillis();
        }
        if (trangThai == null) {
            trangThai = 0; // Mặc định là chờ xác nhận
        }
        if (soTienDauCa == null) soTienDauCa = BigDecimal.ZERO;
        if (soTienCuoiCa == null) soTienCuoiCa = BigDecimal.ZERO;
        if (soTienThuThem == null) soTienThuThem = BigDecimal.ZERO;
        if (soTienChiRa == null) soTienChiRa = BigDecimal.ZERO;
        if (chenhLech == null) chenhLech = BigDecimal.ZERO;
        if (tongDoanhThu == null) tongDoanhThu = BigDecimal.ZERO;
        if (soDonHang == null) soDonHang = 0;
        if (soDonHangThanhToanTienMat == null) soDonHangThanhToanTienMat = 0;
        if (soDonHangThanhToanChuyenKhoan == null) soDonHangThanhToanChuyenKhoan = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        // Sử dụng múi giờ Việt Nam (Asia/Ho_Chi_Minh)
        ZoneId vietnamZone = ZoneId.of("Asia/Ho_Chi_Minh");
        ngayCapNhat = LocalDateTime.now(vietnamZone);
        // Tự động tính chênh lệch
        if (soTienCuoiCa != null && soTienDauCa != null && 
            soTienThuThem != null && soTienChiRa != null) {
            chenhLech = soTienCuoiCa.subtract(soTienDauCa).subtract(soTienThuThem).add(soTienChiRa);
        }
    }
}
