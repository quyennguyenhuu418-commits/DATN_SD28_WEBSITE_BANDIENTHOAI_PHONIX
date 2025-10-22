package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.PhanCaDTO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface PhanCaService {
    List<PhanCaDTO> getAll();
    Optional<PhanCaDTO> getById(Integer id);
    PhanCaDTO save(PhanCaDTO phanCaDTO);
    PhanCaDTO update(Integer id, PhanCaDTO phanCaDTO);
    void delete(Integer id);
    
    List<PhanCaDTO> getByNhanVienId(Integer nhanVienId);
    List<PhanCaDTO> getByCaId(Integer caId);
    List<PhanCaDTO> getByNgayLamViec(LocalDate ngayLamViec);
    List<PhanCaDTO> getByNhanVienIdAndNgayLamViec(Integer nhanVienId, LocalDate ngayLamViec);
    List<PhanCaDTO> getByCaIdAndNgayLamViec(Integer caId, LocalDate ngayLamViec);
    List<PhanCaDTO> getByTrangThai(Integer trangThai);
    List<PhanCaDTO> getByNhanVienIdAndDateRange(Integer nhanVienId, LocalDate startDate, LocalDate endDate);
    List<PhanCaDTO> getByCaIdAndDateRange(Integer caId, LocalDate startDate, LocalDate endDate);
    List<PhanCaDTO> getUpcomingShiftsByNhanVienId(Integer nhanVienId);
    List<PhanCaDTO> getCurrentShiftsByNhanVienId(Integer nhanVienId);
    
    PhanCaDTO startShift(Integer phanCaId);
    PhanCaDTO endShift(Integer phanCaId);
    PhanCaDTO markAbsent(Integer phanCaId, String ghiChu);
    
    boolean existsByNhanVienIdAndNgayLamViecAndCaId(Integer nhanVienId, LocalDate ngayLamViec, Integer caId);
    Map<String, Object> getShiftSummary(Integer shiftId, LocalDate ngayLamViec);
}






