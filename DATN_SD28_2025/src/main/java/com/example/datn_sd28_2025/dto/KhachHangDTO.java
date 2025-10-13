package com.example.datn_sd28_2025.dto;

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
    private String hoTen;
    private String soDienThoai;
    private String taiKhoan;
    private String matKhau;
    private LocalDate ngaySinh;
    private String gioiTinh;
    private String email;
    private String diaChi;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
    private String nguoiTao;
    private String nguoiCapNhat;
}
