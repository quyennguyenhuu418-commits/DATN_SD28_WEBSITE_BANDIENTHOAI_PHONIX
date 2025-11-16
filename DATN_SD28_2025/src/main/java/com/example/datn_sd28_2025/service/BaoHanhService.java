package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.LichSuXuLyBaoHanhDTO;
import com.example.datn_sd28_2025.dto.PhieuBaoHanhDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BaoHanhService {
    // CRUD operations
    List<PhieuBaoHanhDTO> getAll();
    Optional<PhieuBaoHanhDTO> getById(Integer id);
    PhieuBaoHanhDTO create(PhieuBaoHanhDTO phieuBaoHanhDTO);
    PhieuBaoHanhDTO update(Integer id, PhieuBaoHanhDTO phieuBaoHanhDTO);
    
    // Search
    List<PhieuBaoHanhDTO> search(String maPhieu, String tenKhachHang, String soDienThoai, 
                                 String imeiSerial, Integer trangThai, 
                                 LocalDate ngayNhanTu, LocalDate ngayNhanDen);
    
    // Quy trình bảo hành
    PhieuBaoHanhDTO tiepNhanYeuCau(PhieuBaoHanhDTO phieuBaoHanhDTO);
    PhieuBaoHanhDTO kiemTraDieuKienBaoHanh(Integer id, Boolean duDieuKien, String lyDo);
    PhieuBaoHanhDTO danhGiaDieuKien(Integer id, Boolean duDieuKien, String lyDoKhongDu);
    PhieuBaoHanhDTO suaNoiBo(Integer id, String noiDungSuaChua, String ghiChu, Integer nhanVienKyThuatId);
    PhieuBaoHanhDTO guiTTBH(Integer id, String ttbhHang, String maBaoHanhHang);
    PhieuBaoHanhDTO nhanTuTTBH(Integer id, String noiDungSuaChua, String ghiChu);
    PhieuBaoHanhDTO kiemTraQC(Integer id, Boolean qcPass, String ghiChu);
    PhieuBaoHanhDTO traMay(Integer id, String ghiChu);
    PhieuBaoHanhDTO updateStatus(Integer id, Integer trangThai, String hanhDong, String noiDungXuLy);
    
    // Lịch sử xử lý
    List<LichSuXuLyBaoHanhDTO> getLichSuXuLy(Integer phieuBaoHanhId);
    LichSuXuLyBaoHanhDTO addLichSuXuLy(Integer phieuBaoHanhId, LichSuXuLyBaoHanhDTO lichSuDTO);
    
    // Utility methods
    boolean validateImeiWithInvoice(String imei, Integer khachHangId);
    boolean checkWarrantyPeriod(Integer hoaDonId, Integer soNgayBaoHanh);
    String getTrangThaiText(Integer trangThai);
    String getHanhDongText(String hanhDong);
    
    // Statistics
    Long countByTrangThai(Integer trangThai);
    List<PhieuBaoHanhDTO> findByTrangThai(Integer trangThai);
    List<PhieuBaoHanhDTO> findByKhachHangId(Integer khachHangId);
}

