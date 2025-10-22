package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaDTO {
    private Integer id;
    
    @NotBlank(message = "Mã ca không được để trống")
    @Size(max = 20, message = "Mã ca không được quá 20 ký tự")
    private String maCa;
    
    @NotBlank(message = "Tên ca không được để trống")
    @Size(max = 100, message = "Tên ca không được quá 100 ký tự")
    private String tenCa;
    
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String moTa;
    
    @NotNull(message = "Giờ bắt đầu không được để trống")
    private LocalTime gioBatDau;
    
    @NotNull(message = "Giờ kết thúc không được để trống")
    private LocalTime gioKetThuc;
    
    private Integer trangThai;
    private String trangThaiText;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
}

























