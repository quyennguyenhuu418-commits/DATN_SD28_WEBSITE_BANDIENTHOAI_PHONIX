package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "san_pham")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPham {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    @JsonIgnore
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hang")
    @JsonIgnore
    private Hang hang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_man_hinh")
    @JsonIgnore
    private ManHinh manHinh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_camera_truoc")
    @JsonIgnore
    private CameraTruoc cameraTruoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_camera_sau")
    @JsonIgnore
    private CameraSau cameraSau;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chip")
    @JsonIgnore
    private Chip chip;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_gpu")
    @JsonIgnore
    private Gpu gpu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_sim")
    @JsonIgnore
    private Sim sim;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_he_dieu_hanh")
    @JsonIgnore
    private HeDieuHanh heDieuHanh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cpu")
    @JsonIgnore
    private Cpu cpu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pin")
    @JsonIgnore
    private Pin pin;

    @Column(name = "thiet_ke", length = 255)
    private String thietKe;

    @Column(name = "kich_thuoc", length = 100)
    private String kichThuoc;

    @Column(name = "ma_san_pham", unique = true, length = 50)
    private String maSanPham;

    @Column(name = "ten_san_pham", nullable = false, length = 255)
    private String tenSanPham;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

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

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietSanPham> chiTietSanPhams;

    @OneToMany(mappedBy = "sanPham", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Review> reviews;

    // Nhận idDanhMuc, idHang từ JSON để map quan hệ ở service
    @Transient
    @JsonProperty("idDanhMuc")
    private Integer idDanhMuc;

    @Transient
    @JsonProperty("idHang")
    private Integer idHang;

    @Transient
    @JsonProperty("idManHinh")
    private Integer idManHinh;

    @Transient
    @JsonProperty("idCameraTruoc")
    private Integer idCameraTruoc;

    @Transient
    @JsonProperty("idCameraSau")
    private Integer idCameraSau;

    @Transient
    @JsonProperty("idChip")
    private Integer idChip;

    @Transient
    @JsonProperty("idGpu")
    private Integer idGpu;

    @Transient
    @JsonProperty("idSim")
    private Integer idSim;

    @Transient
    @JsonProperty("idHeDieuHanh")
    private Integer idHeDieuHanh;

    @Transient
    @JsonProperty("idCpu")
    private Integer idCpu;

    @Transient
    @JsonProperty("idPin")
    private Integer idPin;
}
