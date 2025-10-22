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
public class LichSuGiaoCaDTO {
    private Integer id;
    
    @NotNull(message = "Giao ca không được để trống")
    private Integer giaoCaId;
    
    @NotBlank(message = "Hành động không được để trống")
    @Size(max = 50, message = "Hành động không được quá 50 ký tự")
    private String hanhDong;
    
    @NotNull(message = "Người thực hiện không được để trống")
    private Integer nguoiThucHienId;
    private String nguoiThucHienTen;
    
    @Size(max = 1000, message = "Nội dung thay đổi không được quá 1000 ký tự")
    private String noiDungThayDoi;
    
    private LocalDateTime thoiGian;
}

























