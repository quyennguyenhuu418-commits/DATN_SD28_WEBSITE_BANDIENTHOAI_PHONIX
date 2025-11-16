package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CauHinhGiaoCaDTO {
    private Integer id;
    
    @NotBlank(message = "Tên cấu hình không được để trống")
    @Size(max = 100, message = "Tên cấu hình không được quá 100 ký tự")
    private String tenCauHinh;
    
    @Size(max = 500, message = "Giá trị không được quá 500 ký tự")
    private String giaTri;
    
    @Size(max = 500, message = "Mô tả không được quá 500 ký tự")
    private String moTa;
    
    @Size(max = 50, message = "Loại cấu hình không được quá 50 ký tự")
    private String loaiCauHinh;
    
    private Integer trangThai;
    private String trangThaiText;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
}





































