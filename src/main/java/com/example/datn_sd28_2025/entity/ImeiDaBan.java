package com.example.datn_sd28_2025.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "imei_da_ban")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImeiDaBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_chi_tiet")
    @JsonIgnore
    private HoaDonCt hoaDonChiTiet;

    @Column(name = "imei", length = 50)
    private String imei;

    @Column(name = "trang_thai")
    private Integer trangThai;

    // Transient field for JSON mapping
    @Transient
    @JsonProperty("idHoaDonChiTiet")
    private Integer idHoaDonChiTiet;
}
