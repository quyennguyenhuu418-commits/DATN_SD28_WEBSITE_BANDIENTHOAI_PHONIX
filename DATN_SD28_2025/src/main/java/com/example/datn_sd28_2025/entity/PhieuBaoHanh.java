package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "phieu_bao_hanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuBaoHanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu", unique = true, length = 50)
    private String maPhieu;

    // Thông tin khách hàng
    @Column(name = "id_khach_hang")
    private Integer khachHangId;

    @Column(name = "ten_khach_hang", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai", length = 15)
    private String soDienThoai;

    // Thông tin sản phẩm
    @Column(name = "id_san_pham")
    private Integer sanPhamId;

    @Column(name = "id_chi_tiet_san_pham")
    private Integer chiTietSanPhamId;

    @Column(name = "id_hoa_don")
    private Integer hoaDonId; // Hóa đơn mua hàng gốc

    @Column(name = "ten_san_pham", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenSanPham;

    @Column(name = "imei_serial", length = 50)
    private String imeiSerial;

    // Tình trạng tiếp nhận
    @Column(name = "mo_ta_loi_khach_hang", columnDefinition = "NTEXT")
    private String moTaLoiKhachHang;

    @Column(name = "mo_ta_loi_nhan_vien", columnDefinition = "NTEXT")
    private String moTaLoiNhanVien;

    @Column(name = "tinh_trang_vat_ly", columnDefinition = "NTEXT")
    private String tinhTrangVatLy; // Trầy xước, cấn móp, v.v.

    @Column(name = "phu_kien_di_kem", length = 500, columnDefinition = "NVARCHAR(500)")
    private String phuKienDiKem; // Sạc, cáp, hộp, v.v.

    // Đánh giá điều kiện bảo hành
    @Column(name = "du_dieu_kien_bao_hanh")
    private Boolean duDieuKienBaoHanh; // true: đủ điều kiện, false: không đủ

    @Column(name = "ly_do_khong_du_dieu_kien", columnDefinition = "NTEXT")
    private String lyDoKhongDuDieuKien;

    // Hướng xử lý
    @Column(name = "huong_xu_ly", length = 50)
    private String huongXuLy; // "SUA_TAI_CUA_HANG", "GUI_TTBH_HANG"

    // Kết quả xử lý
    @Column(name = "noi_dung_sua_chua", columnDefinition = "NTEXT")
    private String noiDungSuaChua;

    @Column(name = "ghi_chu_ky_thuat_vien", columnDefinition = "NTEXT")
    private String ghiChuKyThuatVien;

    @Column(name = "chi_phi_sua_chua", precision = 18, scale = 2)
    private BigDecimal chiPhiSuaChua;

    @Column(name = "khach_da_thanh_toan", precision = 18, scale = 2)
    private BigDecimal khachDaThanhToan;

    // Thông tin TTBH hãng (nếu gửi)
    @Column(name = "ttbh_hang", length = 255, columnDefinition = "NVARCHAR(255)")
    private String ttbhHang; // Tên trung tâm bảo hành hãng

    @Column(name = "ma_bao_hanh_hang", length = 100)
    private String maBaoHanhHang; // Mã bảo hành của hãng

    // Nhân viên xử lý
    @Column(name = "id_nhan_vien_tiep_nhan")
    private Integer nhanVienTiepNhanId; // Nhân viên tiếp nhận

    @Column(name = "id_nhan_vien_ky_thuat")
    private Integer nhanVienKyThuatId; // Kỹ thuật viên xử lý

    @Column(name = "id_nhan_vien_tra_may")
    private Integer nhanVienTraMayId; // Nhân viên trả máy

    // Thời gian
    @Column(name = "ngay_nhan")
    private LocalDate ngayNhan;

    @Column(name = "ngay_hen_tra_du_kien")
    private LocalDate ngayHenTraDuKien;

    @Column(name = "ngay_tra_thuc_te")
    private LocalDate ngayTraThucTe;

    // Trạng thái
    @Column(name = "trang_thai")
    private Integer trangThai; 
    // 0: Mới tiếp nhận / Đang kiểm tra điều kiện
    // 1: Đủ điều kiện bảo hành
    // 2: Không đủ điều kiện bảo hành
    // 3: Đang sửa chữa nội bộ
    // 4: Đã gửi TTBH hãng
    // 5: Đã nhận từ TTBH
    // 6: Đang kiểm tra QC
    // 7: Đã sửa xong
    // 8: Đã trả khách
    // 9: Hoàn tất

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    // Lịch sử xử lý
    @OneToMany(mappedBy = "phieuBaoHanh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LichSuXuLyBaoHanh> lichSuXuLyBaoHanhList;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (maPhieu == null || maPhieu.isEmpty()) {
            maPhieu = generateMaPhieu();
        }
        if (trangThai == null) {
            trangThai = 0; // Mới tiếp nhận
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }

    private String generateMaPhieu() {
        String dateStr = LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        return "BH-" + dateStr + "-" + System.currentTimeMillis() % 10000;
    }
}

