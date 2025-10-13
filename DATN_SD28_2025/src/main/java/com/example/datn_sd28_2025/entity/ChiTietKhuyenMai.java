package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "chi_tiet_khuyen_mai")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietKhuyenMai {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ctsp")
    @JsonIgnore
    private ChiTietSanPham chiTietSanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khuyen_mai")
    @JsonIgnore
    private KhuyenMai khuyenMai;

    @Column(name = "ngay_ap_dung")
    private LocalDateTime ngayApDung;

    @Column(name = "muc_do_uu_tien")
    private Integer mucDoUuTien;

    @Column(name = "phan_tram_giam_gia", precision = 5, scale = 2)
    private BigDecimal phanTramGiamGia;

    // Transient fields for JSON mapping
    @Transient
    @JsonProperty("idCtsp")
    private Integer idCtsp;

    @Transient
    @JsonProperty("idKhuyenMai")
    private Integer idKhuyenMai;
}
