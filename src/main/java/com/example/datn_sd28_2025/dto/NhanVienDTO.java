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
public class NhanVienDTO {
    private Integer id;
    private String maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private LocalDate ngaySinh;
    private String taiKhoan;
    private String matKhau;
    private String gioiTinh;
    private String diaChi;
    private String email;
    private String chucVu;
    private String anhDaiDien;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;
}
