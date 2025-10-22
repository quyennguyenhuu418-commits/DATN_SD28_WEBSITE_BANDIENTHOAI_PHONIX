package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamTrackingDTO {
    private Integer id;
    private String tenSanPham;
    private String hinhAnh;
    private Double gia;
    private Integer soLuong;
    private List<String> imeis;
}