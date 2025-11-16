package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cau_hinh_giao_ca")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauHinhGiaoCa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Tên cấu hình không được để trống")
    @Size(max = 100, message = "Tên cấu hình không được quá 100 ký tự")
    @Column(name = "ten_cau_hinh", unique = true, length = 100)
    private String tenCauHinh;

    @Size(max = 500, message = "Giá trị không được quá 500 ký tự")
    @Column(name = "gia_tri", length = 500, columnDefinition = "NVARCHAR(500)")
    private String giaTri;

    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    @Column(name = "mo_ta", length = 500, columnDefinition = "NVARCHAR(500)")
    private String moTa;

    @Size(max = 50, message = "Loại cấu hình không được quá 50 ký tự")
    @Column(name = "loai_cau_hinh", length = 50)
    private String loaiCauHinh; // TEXT, NUMBER, BOOLEAN, JSON

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

