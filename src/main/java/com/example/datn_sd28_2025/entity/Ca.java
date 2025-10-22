package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "ca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã ca không được để trống")
    @Size(max = 20, message = "Mã ca không được quá 20 ký tự")
    @Column(name = "ma_ca", unique = true, length = 20)
    private String maCa;

    @NotBlank(message = "Tên ca không được để trống")
    @Size(max = 100, message = "Tên ca không được quá 100 ký tự")
    @Column(name = "ten_ca", length = 100, columnDefinition = "NVARCHAR(100)")
    private String tenCa;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    @Column(name = "mo_ta", length = 500, columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @NotNull(message = "Giờ bắt đầu không được để trống")
    @Column(name = "gio_bat_dau")
    private LocalTime gioBatDau;

    @NotNull(message = "Giờ kết thúc không được để trống")
    @Column(name = "gio_ket_thuc")
    private LocalTime gioKetThuc;

    @Column(name = "trang_thai")
    private Integer trangThai; // 1: Hoạt động, 0: Tạm dừng

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

    @OneToMany(mappedBy = "ca", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PhanCa> phanCaList;

    @PrePersist
    protected void onCreate() {
        ngayTao = LocalDateTime.now();
        ngayCapNhat = LocalDateTime.now();
        if (trangThai == null) {
            trangThai = 1; // Mặc định là hoạt động
        }
    }

    @PreUpdate
    protected void onUpdate() {
        ngayCapNhat = LocalDateTime.now();
    }
}

