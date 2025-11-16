package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrangThaiTrackingDTO {
    private Integer trangThai;
    private String tenTrangThai;
    private LocalDateTime thoiGian;
    private String moTa;
    private String actor; // nguoiThucHien
}
