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
@Table(name = "chi_tiet_san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietSanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sp", nullable = false)
    @JsonIgnore
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rom_id")
    @JsonIgnore
    private Rom rom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ram_id")
    @JsonIgnore
    private Ram ram;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mau_sac_id")
    @JsonIgnore
    private MauSac mauSac;


    @Column(name = "ma_ctsp", unique = true, length = 50)
    private String maCtsp;

    @Column(name = "gia_nhap", precision = 18, scale = 2)
    private BigDecimal giaNhap;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "so_luong")
    private Integer soLuong;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "nguoi_tao", length = 100)
    private String nguoiTao;

    @Column(name = "nguoi_cap_nhat", length = 100)
    private String nguoiCapNhat;

    @OneToMany(mappedBy = "chiTietSanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HinhAnh> hinhAnhs;

    @OneToMany(mappedBy = "chiTietSanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Imei> imeis;


    @OneToMany(mappedBy = "chiTietSanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<HoaDonCt> hoaDonCts;

    @OneToMany(mappedBy = "chiTietSanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietKhuyenMai> chiTietKhuyenMais;

    // Transient fields for JSON mapping
    @Transient
    @JsonProperty("idSp")
    public Integer getIdSp() {
        return sanPham != null ? sanPham.getId() : null;
    }

    @Transient
    @JsonProperty("romId")
    public Integer getRomId() {
        return rom != null ? rom.getId() : null;
    }

    @Transient
    @JsonProperty("ramId")
    public Integer getRamId() {
        return ram != null ? ram.getId() : null;
    }

    @Transient
    @JsonProperty("mauSacId")
    public Integer getMauSacId() {
        return mauSac != null ? mauSac.getId() : null;
    }

    // Additional getters for nested object details
    @Transient
    @JsonProperty("ram")
    public Object getRamDetails() {
        if (ram == null) return null;
        return new Object() {
            public Integer getId() { return ram.getId(); }
            public String getTenRam() { return ram.getTenRam(); }
        };
    }

    @Transient
    @JsonProperty("rom")
    public Object getRomDetails() {
        if (rom == null) return null;
        return new Object() {
            public Integer getId() { return rom.getId(); }
            public String getDungLuong() { return rom.getDungLuong(); }
        };
    }

    @Transient
    @JsonProperty("mauSac")
    public Object getMauSacDetails() {
        if (mauSac == null) return null;
        return new Object() {
            public Integer getId() { return mauSac.getId(); }
            public String getTenMau() { return mauSac.getTenMau(); }
            public String getMaHex() { return mauSac.getMaHex(); }
        };
    }
}