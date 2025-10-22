package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "phan_ca", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"nhan_vien_id", "ca_id", "ngay_lam_viec"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Nhân viên không được để trống")
    @Column(name = "nhan_vien_id", nullable = false)
    private Integer nhanVienId;

    @NotNull(message = "Ca không được để trống")
    @Column(name = "ca_id", nullable = false)
    private Integer caId;

    @NotNull(message = "Ngày làm việc không được để trống")
    @Column(name = "ngay_lam_viec", nullable = false)
    private LocalDate ngayLamViec;

    @Column(name = "trang_thai")
    private Integer trangThai; // 0: Chưa bắt đầu, 1: Đang làm, 2: Đã kết thúc, 3: Vắng mặt

    @Column(name = "gio_bat_dau_thuc_te")
    private LocalDateTime gioBatDauThucTe;

    @Column(name = "gio_ket_thuc_thuc_te")
    private LocalDateTime gioKetThucThucTe;

    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    @Column(name = "ghi_chu", length = 500, columnDefinition = "NVARCHAR(500)")
    private String ghiChu;

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
    @JoinColumn(name = "nhan_vien_id", insertable = false, updatable = false)
    private NhanVien nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ca_id", insertable = false, updatable = false)
    private Ca ca;

    @OneToMany(mappedBy = "phanCa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private java.util.List<GiaoCa> giaoCaList;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = 0; // Mặc định là chưa bắt đầu
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}

