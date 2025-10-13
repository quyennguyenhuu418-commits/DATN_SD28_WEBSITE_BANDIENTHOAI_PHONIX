package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "pin")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_pin", length = 50)
    private String maPin;

    @Column(name = "dung_luong_pin", length = 100)
    private String dungLuongPin;

    @Column(name = "cong_nghe_sac", length = 100)
    private String congNgheSac;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "pin", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}
