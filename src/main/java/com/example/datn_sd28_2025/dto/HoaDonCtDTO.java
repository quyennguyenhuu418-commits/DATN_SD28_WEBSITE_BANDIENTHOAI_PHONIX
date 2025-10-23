package com.example.datn_sd28_2025.dto;

import lombok.AllArgsConstructor;
<<<<<<< HEAD
=======
import lombok.Builder;
>>>>>>> origin/Huan
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> origin/Huan

@Data
@NoArgsConstructor
@AllArgsConstructor
<<<<<<< HEAD
=======
@Builder
>>>>>>> origin/Huan
public class HoaDonCtDTO {
    private Integer id;
    private Integer idHoaDon;
    private Integer idCtsp;
    private BigDecimal donGia;
    private BigDecimal thanhTien;
<<<<<<< HEAD
    // Note: ngay_tao and ngay_cap_nhat columns don't exist in the actual database
    // private LocalDateTime ngayTao;
    // private LocalDateTime ngayCapNhat;
=======
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
>>>>>>> origin/Huan
    private Integer trangThai;

    // Related entities
    private HoaDonDTO hoaDon;
    private ChiTietSanPhamDTO chiTietSanPham;
<<<<<<< HEAD
    private List<ImeiDaBanDTO> imeiDaBans;
}
=======
}
>>>>>>> origin/Huan
