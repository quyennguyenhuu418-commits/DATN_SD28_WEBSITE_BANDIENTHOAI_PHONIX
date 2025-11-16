package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.GiaoCaDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface GiaoCaService {
    List<GiaoCaDTO> getAll();
    Optional<GiaoCaDTO> getById(Integer id);
    GiaoCaDTO save(GiaoCaDTO giaoCaDTO);
    GiaoCaDTO update(Integer id, GiaoCaDTO giaoCaDTO);
    void delete(Integer id);
    
    List<GiaoCaDTO> getByPhanCaId(Integer phanCaId);
    List<GiaoCaDTO> getByNhanVienGiaoId(Integer nhanVienGiaoId);
    List<GiaoCaDTO> getByNhanVienNhanId(Integer nhanVienNhanId);
    List<GiaoCaDTO> getByTrangThai(Integer trangThai);
    List<GiaoCaDTO> getByNhanVienId(Integer nhanVienId);
    List<GiaoCaDTO> getByCaId(Integer caId);
    List<GiaoCaDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<GiaoCaDTO> getByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate);
    List<GiaoCaDTO> getByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate);
    List<GiaoCaDTO> getPendingGiaoCaByNhanVienId(Integer nhanVienId);
    List<GiaoCaDTO> getPendingGiaoCaForNhanVienId(Integer nhanVienId);
    List<GiaoCaDTO> getByNhanVienIdAndNgayLamViec(Integer nhanVienId, java.time.LocalDate ngayLamViec);
    List<GiaoCaDTO> getRecentGiaoCa(int limit);
    
    // Business methods
    GiaoCaDTO createGiaoCa(Integer phanCaId, Integer nhanVienGiaoId, BigDecimal soTienDauCa, String ghiChu);
    GiaoCaDTO confirmGiaoCa(Integer giaoCaId, Integer nhanVienNhanId, GiaoCaDTO giaoCaDTO);
    GiaoCaDTO cancelGiaoCa(Integer giaoCaId, String lyDo);
    
    // Statistics
    BigDecimal getTotalRevenueByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate);
    BigDecimal getTotalRevenueByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate);
    Long countGiaoCaByNhanVienIdAndDateRange(Integer nhanVienId, LocalDateTime startDate, LocalDateTime endDate);
    Long countGiaoCaByCaIdAndDateRange(Integer caId, LocalDateTime startDate, LocalDateTime endDate);
    
    boolean existsByMaGiaoCa(String maGiaoCa);
}
