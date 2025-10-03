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
@Table(name = "man_hinh")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ManHinh {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_man_hinh", length = 50)
    private String maManHinh;

    @Column(name = "kich_thuoc", length = 50)
    private String kichThuoc;

    @Column(name = "cong_nghe", length = 100)
    private String congNghe;

    @Column(name = "do_phan_giai", length = 100)
    private String doPhanGiai;

    @Column(name = "tan_so_quet", length = 50)
    private String tanSoQuet;

    @Column(name = "kieu_man_hinh", length = 100)
    private String kieuManHinh;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    @OneToMany(mappedBy = "manHinh", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<SanPham> sanPhams;
}
