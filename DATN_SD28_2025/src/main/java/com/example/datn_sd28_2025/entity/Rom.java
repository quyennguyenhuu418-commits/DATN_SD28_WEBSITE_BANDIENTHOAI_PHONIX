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
@Table(name = "rom")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_rom", length = 50)
    private String maRom;

    @Column(name = "dung_luong", length = 50)
    private String dungLuong;

    @Column(name = "mo_ta", length = 255)
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "rom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<ChiTietSanPham> chiTietSanPhams;
}
