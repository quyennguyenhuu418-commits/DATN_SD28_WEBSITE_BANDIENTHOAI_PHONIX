package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietSanPhamCreateDTO {
    private Integer idSp;
    private Integer ramId;
    private Integer romId;
    private Integer mauSacId;
    private String maCtsp;
    private BigDecimal giaNhap;
    private BigDecimal giaBan;
    private String ghiChu;
    private Integer trangThai;
}
