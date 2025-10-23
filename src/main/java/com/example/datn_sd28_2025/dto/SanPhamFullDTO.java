package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

<<<<<<< HEAD
import java.math.BigDecimal;
=======

>>>>>>> origin/Huan
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SanPhamFullDTO {
    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String thietKe;
    private String kichThuoc;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private Integer trangThai;

    // Related entities
    private DanhMucDTO danhMuc;
    private HangDTO hang;
    private ManHinhDTO manHinh;
    private CameraTruocDTO cameraTruoc;
    private CameraSauDTO cameraSau;
    private ChipDTO chip;
    private GpuDTO gpu;
    private SimDTO sim;
    private HeDieuHanhDTO heDieuHanh;
    private CpuDTO cpu;
    private PinDTO pin;

    // Product variants
    private List<ChiTietSanPhamDTO> chiTietSanPhams;

    // Transient fields for JSON mapping
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
}
