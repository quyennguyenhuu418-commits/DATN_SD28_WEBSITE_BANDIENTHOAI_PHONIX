package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhanCaDTO {
    private Integer id;
    
    @NotNull(message = "Nhân viên không được để trống")
    private Integer nhanVienId;
    private String nhanVienTen;
    private String nhanVienMa;
    private NhanVienInfo nhanVien;
    
    @NotNull(message = "Ca không được để trống")
    private Integer caId;
    private String caTen;
    private String caMa;
    private CaInfo ca;
    
    @NotNull(message = "Ngày làm việc không được để trống")
    private LocalDate ngayLamViec;
    
    private Integer trangThai;
    private String trangThaiText;
    private LocalDateTime gioBatDauThucTe;
    private LocalDateTime gioKetThucThucTe;
    
    @Size(max = 500, message = "Ghi chú không được quá 500 ký tự")
    private String ghiChu;
    
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class NhanVienInfo {
        private Integer id;
        private String hoTen;
        private String maNhanVien;
        private String email;
        private String soDienThoai;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CaInfo {
        private Integer id;
        private String tenCa;
        private LocalTime gioBatDau;
        private LocalTime gioKetThuc;
        private String moTa;
        private Integer trangThai;
    }
}
