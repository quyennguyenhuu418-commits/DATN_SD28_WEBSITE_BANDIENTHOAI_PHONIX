package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KhachHangDTO {
    private Integer id;
    private String maKhachHang;
    @NotBlank(message = "Họ tên không được để trống")
    @Size(min = 2, max = 255, message = "Họ tên phải có từ 2 đến 255 ký tự")
    @Pattern(regexp = "^[\\p{L}\\s]+$", 
             message = "Họ tên chỉ được chứa chữ cái và khoảng trắng")
    private String hoTen;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[3|5|7|8|9])+([0-9]{8})$", message = "Số điện thoại phải bắt đầu bằng 0 và có 10 chữ số")
    private String soDienThoai;
    
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate ngaySinh;
    
    @Pattern(regexp = "^(Nam|Nữ|Khác|)$", message = "Giới tính phải là Nam, Nữ hoặc Khác")
    private String gioiTinh;
    
    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    @Size(max = 255, message = "Email không được quá 255 ký tự")
    private String email;
    
    // Address information - populated from user_dia_chi table
    private String diaChi;
    
    private String taiKhoan;
    
    private String matKhau;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
    private String nguoiTao;
    private String nguoiCapNhat;
}
