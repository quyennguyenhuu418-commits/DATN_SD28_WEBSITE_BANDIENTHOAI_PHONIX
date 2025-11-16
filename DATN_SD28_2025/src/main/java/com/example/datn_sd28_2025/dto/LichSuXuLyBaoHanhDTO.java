package com.example.datn_sd28_2025.dto;

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
public class LichSuXuLyBaoHanhDTO {
    private Integer id;
    private Integer idPhieuBaoHanh;
    private LocalDateTime thoiGian;
    private Integer nhanVienThucHienId;
    private String tenNhanVienThucHien;
    private String hanhDong;
    private String hanhDongText; // Hành động dạng text để hiển thị
    private String noiDungXuLy;
    private BigDecimal chiPhiPhatSinh;
    private String linhKienThayThe;
    private String ghiChu;
    private Integer trangThaiTruoc;
    private Integer trangThaiSau;
    private String trangThaiTruocText;
    private String trangThaiSauText;
    private LocalDateTime ngayTao;
}

