package com.example.datn_sd28_2025.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GiaoCaDTO {
    private Integer id;
    
    @NotBlank(message = "Mã giao ca không được để trống")
    @Size(max = 50, message = "Mã giao ca không được quá 50 ký tự")
    private String maGiaoCa;
    
    @NotNull(message = "Phân ca không được để trống")
    private Integer phanCaId;
    private String nhanVienTen;
    private String caTen;
    private LocalDate ngayLamViec;
    
    @NotNull(message = "Nhân viên giao không được để trống")
    private Integer nhanVienGiaoId;
    private String nhanVienGiaoTen;
    private NhanVienInfo nhanVienGiao;
    
    private Integer nhanVienNhanId;
    private String nhanVienNhanTen;
    private NhanVienInfo nhanVienNhan;
    
    @NotNull(message = "Ngày giao ca không được để trống")
    private LocalDateTime ngayGiaoCa;
    
    // Thông tin tiền mặt
    @NotNull(message = "Số tiền đầu ca không được để trống")
    @DecimalMin(value = "0.0", message = "Số tiền đầu ca phải lớn hơn hoặc bằng 0")
    private BigDecimal soTienDauCa;
    private BigDecimal soTienCuoiCa;
    private BigDecimal soTienThuThem;
    private BigDecimal soTienChiRa;
    private BigDecimal chenhLech;
    
    // Thông tin doanh thu ca
    private BigDecimal tongDoanhThu;
    private Integer soDonHang;
    private Integer soDonHangThanhToanTienMat;
    private Integer soDonHangThanhToanChuyenKhoan;
    
    // Trạng thái giao ca
    private Integer trangThai;
    private String trangThaiText;
    private LocalDateTime thoiGianXacNhan;
    
    // Ghi chú và báo cáo
    @Size(max = 1000, message = "Ghi chú không được quá 1000 ký tự")
    private String ghiChu;
    
    @Size(max = 2000, message = "Báo cáo công việc không được quá 2000 ký tự")
    private String baoCaoCongViec;
    
    @Size(max = 1000, message = "Sự cố bất thường không được quá 1000 ký tự")
    private String suCoBatThuong;
    
    @Size(max = 1000, message = "Công việc tồn đọng không được quá 1000 ký tự")
    private String congViecTonDong;
    
    private LocalDateTime ngayTao;
    private LocalDateTime ngayCapNhat;
    private String nguoiTao;
    private String nguoiCapNhat;
    
    // Chi tiết giao ca
    private List<ChiTietGiaoCaDTO> chiTietGiaoCaList;
    
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
}
