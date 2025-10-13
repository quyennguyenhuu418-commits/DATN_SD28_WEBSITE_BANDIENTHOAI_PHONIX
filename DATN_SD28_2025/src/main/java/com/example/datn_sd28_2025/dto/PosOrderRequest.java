package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PosOrderRequest {
    private HoaDonDTO hoaDon;
    private List<ChiTietHoaDonDTO> chiTietHoaDon;
}




