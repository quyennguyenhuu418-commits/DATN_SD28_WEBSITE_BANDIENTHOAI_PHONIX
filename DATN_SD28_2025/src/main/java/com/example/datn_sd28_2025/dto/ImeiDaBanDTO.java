package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImeiDaBanDTO {
    private Integer id;
    private Integer idHoaDonChiTiet;
    private String imei;
    private Integer trangThai;

    // Related entities
    private HoaDonCtDTO hoaDonChiTiet;
}
