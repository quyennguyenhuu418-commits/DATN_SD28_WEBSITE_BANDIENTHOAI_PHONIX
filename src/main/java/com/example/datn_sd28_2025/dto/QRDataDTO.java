package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QRDataDTO {
    
    @NotBlank(message = "Số căn cước không được để trống")
    @Size(max = 20, message = "Số căn cước không được quá 20 ký tự")
    private String cccd;
    
    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 255, message = "Họ tên không được quá 255 ký tự")
    private String hoTen;
    
    @NotBlank(message = "Ngày sinh không được để trống")
    private String ngaySinh;
    
    @NotBlank(message = "Giới tính không được để trống")
    @Size(max = 10, message = "Giới tính không được quá 10 ký tự")
    private String gioiTinh;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    @Size(max = 1000, message = "Địa chỉ không được quá 1000 ký tự")
    private String diaChi;
}