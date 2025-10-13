package com.example.datn_sd28_2025.service.dto;

import lombok.Data;
import java.util.List;

@Data
public class SanPhamFullRequest {
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String thietKe;
    private String kichThuoc;
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
    private List<Variant> variants;

    @Data
    public static class Variant {
        private Integer idRam;
        private Integer idRom;
        private Integer idMauSac;
        private Integer soLuong;
        private Long donGia;
        private Long giaNhap;
        private String ghiChu;
        private List<String> imeis;
        private List<String> imageUrls; 
    }
}
