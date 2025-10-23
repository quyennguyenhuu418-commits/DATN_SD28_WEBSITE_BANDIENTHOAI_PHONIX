package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> origin/Huan

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuGiamGiaDTO {
    private Integer id;
    private String maPhieuGiamGia;
    private String tenPhieuGiamGia;
    private String loaiPhieuGiamGia;
    private BigDecimal giaTriGiamGia;
    private BigDecimal soTienGiamToiDa;
    private BigDecimal hoaDonToiThieu;
    private Integer soLuongDung;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private Integer trangThai;
    private Boolean riengTu;
    private String moTa;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
<<<<<<< HEAD
    private List<Integer> selectedCustomers; // Danh sách ID khách hàng được chọn cho voucher riêng tư
=======
>>>>>>> origin/Huan
}
