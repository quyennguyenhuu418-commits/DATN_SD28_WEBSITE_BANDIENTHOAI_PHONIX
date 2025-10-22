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
@Table(name = "imei")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Imei {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ctsp", nullable = false)
    @JsonIgnore
    private ChiTietSanPham chiTietSanPham;

    @Column(name = "imei", length = 50)
    private String imei;

    // Note: ngay_tao and ngay_cap_nhat columns don't exist in the actual database
    // @Column(name = "ngay_tao")
    // private LocalDateTime ngayTao;

    // @Column(name = "ngay_cap_nhat")
    // private LocalDateTime ngayCapNhat;

    @Column(name = "trang_thai")
    private Integer trangThai;

    // Transient field for JSON mapping
    @Transient
    @JsonProperty("idCtsp")
    private Integer idCtsp;
}
