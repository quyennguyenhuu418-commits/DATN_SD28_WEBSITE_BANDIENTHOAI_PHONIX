package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietSanPhamDTO {
    private Integer id;
    private String maCtsp;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private Integer soLuong;
    private String ghiChu;
    private Integer trangThai;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;

    // Related entities
    private SanPhamDTO sanPham;
    private RomDTO rom;
    private RamDTO ram;
    private MauSacDTO mauSac;

    // Images and IMEIs
    private List<HinhAnhDTO> hinhAnhs;
    private List<ImeiDTO> imeis;

    // Transient fields for JSON mapping
    private Integer idSp;
    private Integer romId;
    private Integer ramId;
    private Integer mauSacId;
}
