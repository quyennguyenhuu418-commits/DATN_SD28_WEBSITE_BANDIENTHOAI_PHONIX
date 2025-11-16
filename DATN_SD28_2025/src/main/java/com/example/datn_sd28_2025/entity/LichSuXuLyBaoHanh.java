package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lich_su_xu_ly_bao_hanh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LichSuXuLyBaoHanh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_bao_hanh", nullable = false)
    @JsonIgnore
    private PhieuBaoHanh phieuBaoHanh;

    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;

    @Column(name = "id_nhan_vien_thuc_hien")
    private Integer nhanVienThucHienId;

    @Column(name = "ten_nhan_vien_thuc_hien", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenNhanVienThucHien;

    @Column(name = "hanh_dong", length = 100)
    private String hanhDong; 
    // "TIEP_NHAN", "KIEM_TRA_DIEU_KIEN", "DU_DIEU_KIEN", "KHONG_DU_DIEU_KIEN",
    // "CHAN_DOAN_LOI", "SUA_NOI_BO", "GUI_TTBH", "NHAN_TU_TTBH", 
    // "KIEM_TRA_QC", "TRA_MAY", "HOAN_TAT"

    @Column(name = "noi_dung_xu_ly", columnDefinition = "NTEXT")
    private String noiDungXuLy; // Mô tả chi tiết hành động

    @Column(name = "chi_phi_phat_sinh", precision = 18, scale = 2)
    private BigDecimal chiPhiPhatSinh;

    @Column(name = "linh_kien_thay_the", length = 500, columnDefinition = "NVARCHAR(500)")
    private String linhKienThayThe; // Danh sách linh kiện đã thay thế

    @Column(name = "ghi_chu", columnDefinition = "NTEXT")
    private String ghiChu;

    @Column(name = "trang_thai_truoc")
    private Integer trangThaiTruoc;

    @Column(name = "trang_thai_sau")
    private Integer trangThaiSau;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }

    // Transient field for JSON mapping
    @Transient
    @com.fasterxml.jackson.annotation.JsonProperty("idPhieuBaoHanh")
    private Integer idPhieuBaoHanh;
}

