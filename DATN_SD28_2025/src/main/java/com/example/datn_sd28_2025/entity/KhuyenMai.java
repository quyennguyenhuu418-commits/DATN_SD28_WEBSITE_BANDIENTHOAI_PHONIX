package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "khuyen_mai")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai", unique = true, length = 50)
    private String maKhuyenMai;

    @Column(name = "ten_khuyen_mai", nullable = false, length = 255)
    private String tenKhuyenMai;

    @Column(name = "mo_ta", length = 500)
    private String moTa;


    @Column(name = "phan_tram_giam", precision = 5, scale = 2)
    private BigDecimal phanTramGiam;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 255)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 255)
    private String nguoiCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "khuyenMai", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietKhuyenMai> chiTietKhuyenMais;
}
