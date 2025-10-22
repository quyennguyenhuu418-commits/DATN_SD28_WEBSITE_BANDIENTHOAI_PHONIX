package com.example.datn_sd28_2025.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dia_chi")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaChi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "dia_chi_chi_tiet", length = 500, columnDefinition = "NVARCHAR(500)")
    private String diaChiChiTiet;

    @Column(name = "phuong_xa", length = 100, columnDefinition = "NVARCHAR(100)")
    private String phuongXa;

    @Column(name = "quan_huyen", length = 100, columnDefinition = "NVARCHAR(100)")
    private String quanHuyen;

    @Column(name = "tinh_thanh_pho", length = 100, columnDefinition = "NVARCHAR(100)")
    private String tinhThanhPho;

    @Column(name = "ma_buu_dien", length = 20)
    private String maBuuDien;

    @Column(name = "ghi_chu", length = 500, columnDefinition = "NVARCHAR(500)")
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;
}





