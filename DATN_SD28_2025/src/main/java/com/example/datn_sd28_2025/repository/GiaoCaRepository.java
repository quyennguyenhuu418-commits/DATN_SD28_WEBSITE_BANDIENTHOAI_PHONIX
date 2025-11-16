package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.GiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GiaoCaRepository extends JpaRepository<GiaoCa, Integer> {
    
    Optional<GiaoCa> findByMaGiaoCa(String maGiaoCa);
    
    List<GiaoCa> findByPhanCaId(Integer phanCaId);
    
    List<GiaoCa> findByNhanVienGiaoId(Integer nhanVienGiaoId);
    
    List<GiaoCa> findByNhanVienNhanId(Integer nhanVienNhanId);
    
    List<GiaoCa> findByTrangThai(Integer trangThai);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCa.nhanVienId = :nhanVienId ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByNhanVienId(@Param("nhanVienId") Integer nhanVienId);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCa.caId = :caId ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByCaId(@Param("caId") Integer caId);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.ngayGiaoCa BETWEEN :startDate AND :endDate ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                 @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCa.nhanVienId = :nhanVienId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByNhanVienIdAndDateRange(@Param("nhanVienId") Integer nhanVienId, 
                                              @Param("startDate") LocalDateTime startDate, 
                                              @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCa.caId = :caId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByCaIdAndDateRange(@Param("caId") Integer caId, 
                                        @Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCaId = :phanCaId AND gc.trangThai = :trangThai")
    Optional<GiaoCa> findByPhanCaIdAndTrangThai(@Param("phanCaId") Integer phanCaId, 
                                                @Param("trangThai") Integer trangThai);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.nhanVienGiaoId = :nhanVienId AND gc.trangThai = 0 ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findPendingGiaoCaByNhanVienId(@Param("nhanVienId") Integer nhanVienId);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.nhanVienNhanId = :nhanVienId AND gc.trangThai = 0 ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findPendingGiaoCaForNhanVienId(@Param("nhanVienId") Integer nhanVienId);
    
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCa.nhanVienId = :nhanVienId AND gc.phanCa.ngayLamViec = :ngayLamViec ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findByNhanVienIdAndNgayLamViec(@Param("nhanVienId") Integer nhanVienId, 
                                                @Param("ngayLamViec") java.time.LocalDate ngayLamViec);
    
    @Query("SELECT SUM(gc.tongDoanhThu) FROM GiaoCa gc WHERE gc.phanCa.nhanVienId = :nhanVienId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate AND gc.trangThai = 1")
    java.math.BigDecimal getTotalRevenueByNhanVienIdAndDateRange(@Param("nhanVienId") Integer nhanVienId, 
                                                                @Param("startDate") LocalDateTime startDate, 
                                                                @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(gc.tongDoanhThu) FROM GiaoCa gc WHERE gc.phanCa.caId = :caId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate AND gc.trangThai = 1")
    java.math.BigDecimal getTotalRevenueByCaIdAndDateRange(@Param("caId") Integer caId, 
                                                          @Param("startDate") LocalDateTime startDate, 
                                                          @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(gc) FROM GiaoCa gc WHERE gc.phanCa.nhanVienId = :nhanVienId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate AND gc.trangThai = 1")
    long countGiaoCaByNhanVienIdAndDateRange(@Param("nhanVienId") Integer nhanVienId, 
                                             @Param("startDate") LocalDateTime startDate, 
                                             @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(gc) FROM GiaoCa gc WHERE gc.phanCa.caId = :caId AND gc.ngayGiaoCa BETWEEN :startDate AND :endDate AND gc.trangThai = 1")
    long countGiaoCaByCaIdAndDateRange(@Param("caId") Integer caId, 
                                       @Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    boolean existsByMaGiaoCa(String maGiaoCa);
    
    @Query("SELECT gc FROM GiaoCa gc LEFT JOIN FETCH gc.phanCa pc LEFT JOIN FETCH pc.nhanVien LEFT JOIN FETCH gc.nhanVienGiao LEFT JOIN FETCH gc.nhanVienNhan ORDER BY gc.ngayGiaoCa DESC")
    List<GiaoCa> findTop10ByOrderByNgayGiaoCaDesc();
    
    // Find previous giao ca for the same phan ca or same ca on previous day
    @Query("SELECT gc FROM GiaoCa gc WHERE gc.phanCaId = :phanCaId AND gc.trangThai = 1 ORDER BY gc.ngayGiaoCa DESC")
    Optional<GiaoCa> findLastConfirmedGiaoCaByPhanCaId(@Param("phanCaId") Integer phanCaId);
    
    // Find last confirmed giao ca for a ca on the same or previous day
    @Query("SELECT gc FROM GiaoCa gc LEFT JOIN FETCH gc.phanCa pc WHERE pc.caId = :caId AND gc.trangThai = 1 AND pc.ngayLamViec <= :ngayLamViec ORDER BY pc.ngayLamViec DESC, gc.ngayGiaoCa DESC")
    List<GiaoCa> findLastConfirmedGiaoCaByCaIdAndDate(@Param("caId") Integer caId, 
                                                        @Param("ngayLamViec") java.time.LocalDate ngayLamViec);
}
