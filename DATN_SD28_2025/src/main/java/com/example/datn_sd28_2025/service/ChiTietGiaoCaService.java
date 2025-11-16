package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ChiTietGiaoCaDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ChiTietGiaoCaService {
    List<ChiTietGiaoCaDTO> getAll();
    Optional<ChiTietGiaoCaDTO> getById(Integer id);
    ChiTietGiaoCaDTO save(ChiTietGiaoCaDTO chiTietGiaoCaDTO);
    ChiTietGiaoCaDTO update(Integer id, ChiTietGiaoCaDTO chiTietGiaoCaDTO);
    void delete(Integer id);
    
    List<ChiTietGiaoCaDTO> getByGiaoCaId(Integer giaoCaId);
    List<ChiTietGiaoCaDTO> getByLoaiChiTiet(String loaiChiTiet);
    List<ChiTietGiaoCaDTO> getByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet);
    List<ChiTietGiaoCaDTO> getByTrangThai(Integer trangThai);
    List<ChiTietGiaoCaDTO> getActiveByGiaoCaId(Integer giaoCaId);
    List<ChiTietGiaoCaDTO> getActiveByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet);
    List<ChiTietGiaoCaDTO> getByNhanVienIdAndLoaiChiTiet(Integer nhanVienId, String loaiChiTiet);
    List<ChiTietGiaoCaDTO> getByCaIdAndLoaiChiTiet(Integer caId, String loaiChiTiet);
    
    // Statistics
    BigDecimal getTotalValueByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet);
    Long getTotalQuantityByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet);
    
    // Business methods
    ChiTietGiaoCaDTO addDoanhThu(Integer giaoCaId, String tenChiTiet, BigDecimal giaTri, String moTa);
    ChiTietGiaoCaDTO addDonHang(Integer giaoCaId, String tenChiTiet, Integer soLuong, String moTa);
    ChiTietGiaoCaDTO addTienMat(Integer giaoCaId, String tenChiTiet, BigDecimal giaTri, String moTa);
    ChiTietGiaoCaDTO addSuCo(Integer giaoCaId, String tenChiTiet, String moTa);
    ChiTietGiaoCaDTO addCongViec(Integer giaoCaId, String tenChiTiet, String moTa);
}









































