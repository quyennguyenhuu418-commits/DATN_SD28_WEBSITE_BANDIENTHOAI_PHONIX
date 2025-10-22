package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamEditDTO {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String thietKe;
    private String kichThuoc;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // ID fields for editing
    private Integer idDanhMuc;
    private Integer idHang;
    private Integer idManHinh;
    private Integer idCameraTruoc;
    private Integer idCameraSau;
    private Integer idChip;
    private Integer idGpu;
    private Integer idSim;
    private Integer idHeDieuHanh;
    private Integer idCpu;
    private Integer idPin;

    // Name fields for display
    private String tenDanhMuc;
    private String tenHang;
    private String tenManHinh;
    private String tenCameraTruoc;
    private String tenCameraSau;
    private String tenChip;
    private String tenGpu;
    private String tenSim;
    private String tenHeDieuHanh;
    private String tenCpu;
    private String tenPin;

    // Variants for editing
    private List<VariantEditDTO> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantEditDTO {
        private Integer id;
        private Integer idRam;
        private Integer idRom;
        private Integer idMauSac;
        private Integer soLuong;
        private Long donGia;
        private Long giaNhap;
        private String ghiChu;
        private List<String> imeis;
        private List<String> imageUrls;
        
        // Name fields for display
        private String tenRam;
        private String tenRom;
        private String tenMauSac;
    }
}
