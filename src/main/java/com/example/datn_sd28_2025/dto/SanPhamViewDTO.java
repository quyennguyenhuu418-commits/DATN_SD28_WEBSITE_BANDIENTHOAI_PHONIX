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
public class SanPhamViewDTO {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String thietKe;
    private String kichThuoc;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // Flattened fields for display
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

    // Aggregated fields
    private Integer tongImei;
    
    // Variants for viewing
    private List<VariantViewDTO> variants;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VariantViewDTO {
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
