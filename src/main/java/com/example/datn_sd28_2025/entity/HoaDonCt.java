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
import java.util.List;

@Entity
@Table(name = "hoa_don_chi_tiet")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonCt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    @JsonIgnore
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ctsp", nullable = false)
    @JsonIgnore
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "thanh_tien", precision = 18, scale = 2)
    private BigDecimal thanhTien;

<<<<<<< HEAD
    // Note: ngay_tao and ngay_cap_nhat columns don't exist in the actual database
    // @Column(name = "ngay_tao")
    // private LocalDateTime ngayTao;

    // @Column(name = "ngay_cap_nhat")
    // private LocalDateTime ngayCapNhat;
=======
    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
>>>>>>> origin/Huan

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "hoaDonChiTiet", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ImeiDaBan> imeiDaBans;

    // Transient fields for JSON mapping
    @Transient
    @JsonProperty("idHoaDon")
    private Integer idHoaDon;

    @Transient
    @JsonProperty("idCtsp")
    private Integer idCtsp;
}
