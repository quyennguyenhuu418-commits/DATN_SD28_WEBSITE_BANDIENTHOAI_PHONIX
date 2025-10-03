package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "khach_hang_giam_gia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangGiamGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang")
    @JsonIgnore
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_giam_gia")
    @JsonIgnore
    private PhieuGiamGia phieuGiamGia;

    @Column(name = "nguoi_su_dung", length = 255)
    private String nguoiSuDung;

    @Column(name = "ngay_cap")
    private LocalDateTime ngayCap;

    @Column(name = "trang_thai")
    private Integer trangThai;

    // Transient fields for JSON mapping
    @Transient
    @JsonProperty("idKhachHang")
    private Integer idKhachHang;

    @Transient
    @JsonProperty("idPhieuGiamGia")
    private Integer idPhieuGiamGia;
}
