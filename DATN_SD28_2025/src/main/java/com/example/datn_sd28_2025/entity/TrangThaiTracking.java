package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "trang_thai_tracking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrangThaiTracking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @Column(name = "hoa_don_id", nullable = false)
    private Integer hoaDonId;
    
    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;
    
    @Column(name = "ten_trang_thai")
    private String tenTrangThai;
    
    @Column(name = "thoi_gian", nullable = false)
    private LocalDateTime thoiGian;
    
    @Column(name = "mo_ta")
    private String moTa;
    
    @Column(name = "nguoi_thuc_hien")
    private String nguoiThucHien;
    
    // Constructor để tạo mới
    public TrangThaiTracking(Integer hoaDonId, Integer trangThai, String tenTrangThai, String moTa, String nguoiThucHien) {
        this.hoaDonId = hoaDonId;
        this.trangThai = trangThai;
        this.tenTrangThai = tenTrangThai;
        this.thoiGian = LocalDateTime.now();
        this.moTa = moTa;
        this.nguoiThucHien = nguoiThucHien;
    }
}


