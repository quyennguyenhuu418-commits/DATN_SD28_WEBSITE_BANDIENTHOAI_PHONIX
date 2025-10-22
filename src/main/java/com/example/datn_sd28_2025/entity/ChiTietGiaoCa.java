package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_giao_ca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGiaoCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull(message = "Giao ca không được để trống")
    @Column(name = "giao_ca_id", nullable = false)
    private Integer giaoCaId;

    @NotBlank(message = "Loại chi tiết không được để trống")
    @Size(max = 50, message = "Loại chi tiết không được quá 50 ký tự")
    @Column(name = "loai_chi_tiet", length = 50)
    private String loaiChiTiet; // DOANH_THU, DON_HANG, TIEN_MAT, SU_CO, CONG_VIEC

    @NotBlank(message = "Tên chi tiết không được để trống")
    @Size(max = 255, message = "Tên chi tiết không được quá 255 ký tự")
    @Column(name = "ten_chi_tiet", length = 255, columnDefinition = "NVARCHAR(255)")
    private String tenChiTiet;

    @Column(name = "gia_tri", precision = 18, scale = 2)
    private BigDecimal giaTri;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    @Column(name = "mo_ta", length = 500, columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;

    @Column(name = "trang_thai")
    private Integer trangThai; // 1: Hoạt động, 0: Tạm dừng

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    // Relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "giao_ca_id", insertable = false, updatable = false)
    private GiaoCa giaoCa;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = 1; // Mặc định là hoạt động
        }
        if (thoiGian == null) {
            thoiGian = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}

