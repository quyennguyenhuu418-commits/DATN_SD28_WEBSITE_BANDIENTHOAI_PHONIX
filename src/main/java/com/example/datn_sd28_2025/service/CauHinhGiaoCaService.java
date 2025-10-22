package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CauHinhGiaoCaDTO;

import java.util.List;
import java.util.Optional;

public interface CauHinhGiaoCaService {
    List<CauHinhGiaoCaDTO> getAll();
    Optional<CauHinhGiaoCaDTO> getById(Integer id);
    CauHinhGiaoCaDTO save(CauHinhGiaoCaDTO cauHinhGiaoCaDTO);
    CauHinhGiaoCaDTO update(Integer id, CauHinhGiaoCaDTO cauHinhGiaoCaDTO);
    void delete(Integer id);
    
    Optional<CauHinhGiaoCaDTO> getByTenCauHinh(String tenCauHinh);
    List<CauHinhGiaoCaDTO> getByTrangThai(Integer trangThai);
    List<CauHinhGiaoCaDTO> getByLoaiCauHinh(String loaiCauHinh);
    List<CauHinhGiaoCaDTO> getActiveConfigurations();
    List<CauHinhGiaoCaDTO> searchByTenCauHinh(String tenCauHinh);
    List<CauHinhGiaoCaDTO> getActiveByLoaiCauHinh(String loaiCauHinh);
    
    boolean existsByTenCauHinh(String tenCauHinh);
    boolean existsByTenCauHinhAndIdNot(String tenCauHinh, Integer id);
    
    // Configuration methods
    String getStringValue(String tenCauHinh, String defaultValue);
    Integer getIntValue(String tenCauHinh, Integer defaultValue);
    Boolean getBooleanValue(String tenCauHinh, Boolean defaultValue);
    void setValue(String tenCauHinh, String value);
}

























