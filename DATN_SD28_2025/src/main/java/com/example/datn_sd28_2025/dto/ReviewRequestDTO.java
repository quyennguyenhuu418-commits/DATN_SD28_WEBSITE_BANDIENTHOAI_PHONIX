package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewRequestDTO {
    @NotNull(message = "ID sản phẩm không được để trống")
    private Integer idSanPham;
    
    // ID người dùng có thể null cho guest
    private Integer idNguoiDung;
    
    // Thông tin guest (chỉ cần khi idNguoiDung = null)
    private String tenNguoiDung;
    
    @NotNull(message = "Đánh giá không được để trống")
    @Min(value = 1, message = "Đánh giá phải từ 1 đến 5 sao")
    @Max(value = 5, message = "Đánh giá phải từ 1 đến 5 sao")
    private Integer rating;
    
    @Size(max = 500, message = "Bình luận không được vượt quá 500 ký tự")
    private String comment;
}







