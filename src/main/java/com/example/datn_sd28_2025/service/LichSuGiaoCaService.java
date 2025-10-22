package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.LichSuGiaoCaDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface LichSuGiaoCaService {
    List<LichSuGiaoCaDTO> getAll();
    LichSuGiaoCaDTO getById(Integer id);
    LichSuGiaoCaDTO save(LichSuGiaoCaDTO lichSuGiaoCaDTO);
    LichSuGiaoCaDTO update(Integer id, LichSuGiaoCaDTO lichSuGiaoCaDTO);
    void delete(Integer id);
    
    List<LichSuGiaoCaDTO> getByGiaoCaId(Integer giaoCaId);
    List<LichSuGiaoCaDTO> getByNguoiThucHienId(Integer nguoiThucHienId);
    List<LichSuGiaoCaDTO> getByHanhDong(String hanhDong);
    List<LichSuGiaoCaDTO> getByGiaoCaIdOrderByThoiGianDesc(Integer giaoCaId);
    List<LichSuGiaoCaDTO> getByNguoiThucHienIdOrderByThoiGianDesc(Integer nguoiThucHienId);
    List<LichSuGiaoCaDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<LichSuGiaoCaDTO> getByGiaoCaIdAndHanhDong(Integer giaoCaId, String hanhDong);
    List<LichSuGiaoCaDTO> getByNguoiThucHienIdAndDateRange(Integer nguoiThucHienId, LocalDateTime startDate, LocalDateTime endDate);
    List<LichSuGiaoCaDTO> getByNhanVienId(Integer nhanVienId);
    List<LichSuGiaoCaDTO> getByCaId(Integer caId);
    
    // Business methods
    LichSuGiaoCaDTO logAction(Integer giaoCaId, String hanhDong, Integer nguoiThucHienId, String noiDungThayDoi);
}

























