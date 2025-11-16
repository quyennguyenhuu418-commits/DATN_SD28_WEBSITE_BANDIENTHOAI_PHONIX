package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChiTietGiaoCaDTO {
    private Integer id;
    
    @NotNull(message = "Giao ca không được để trống")
    private Integer giaoCaId;
    
    @NotBlank(message = "Loại chi tiết không được để trống")
    @Size(max = 50, message = "Loại chi tiết không được quá 50 ký tự")
    private String loaiChiTiet;
    
    @NotBlank(message = "Tên chi tiết không được để trống")
    @Size(max = 255, message = "Tên chi tiết không được quá 255 ký tự")
    private String tenChiTiet;
    
    private BigDecimal giaTri;
    private Integer soLuong;
    
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String moTa;
    
    private LocalDateTime thoiGian;
    private Integer trangThai;
    private String trangThaiText;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
}







































