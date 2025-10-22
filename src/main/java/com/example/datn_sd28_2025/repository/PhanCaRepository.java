package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.PhanCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhanCaRepository extends JpaRepository<PhanCa, Integer> {
    
    @Query("SELECT pc FROM PhanCa pc LEFT JOIN FETCH pc.nhanVien LEFT JOIN FETCH pc.ca ORDER BY pc.ngayLamViec DESC, pc.ca.gioBatDau")
    List<PhanCa> findAllWithRelations();
    
    List<PhanCa> findByNhanVienId(Integer nhanVienId);
    
    List<PhanCa> findByCaId(Integer caId);
    
    List<PhanCa> findByNgayLamViec(LocalDate ngayLamViec);
    
    List<PhanCa> findByNhanVienIdAndNgayLamViec(Integer nhanVienId, LocalDate ngayLamViec);
    
    List<PhanCa> findByCaIdAndNgayLamViec(Integer caId, LocalDate ngayLamViec);
    
    List<PhanCa> findByTrangThai(Integer trangThai);
    
    @Query("SELECT pc FROM PhanCa pc LEFT JOIN FETCH pc.nhanVien LEFT JOIN FETCH pc.ca WHERE pc.nhanVienId = :nhanVienId AND pc.ngayLamViec BETWEEN :startDate AND :endDate ORDER BY pc.ngayLamViec, pc.ca.gioBatDau")
    List<PhanCa> findByNhanVienIdAndDateRange(@Param("nhanVienId") Integer nhanVienId, 
                                              @Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pc FROM PhanCa pc LEFT JOIN FETCH pc.nhanVien LEFT JOIN FETCH pc.ca WHERE pc.caId = :caId AND pc.ngayLamViec BETWEEN :startDate AND :endDate ORDER BY pc.ngayLamViec")
    List<PhanCa> findByCaIdAndDateRange(@Param("caId") Integer caId, 
                                        @Param("startDate") LocalDate startDate, 
                                        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT pc FROM PhanCa pc WHERE pc.ngayLamViec = :ngayLamViec AND pc.trangThai = :trangThai ORDER BY pc.ca.gioBatDau")
    List<PhanCa> findByNgayLamViecAndTrangThai(@Param("ngayLamViec") LocalDate ngayLamViec, 
                                               @Param("trangThai") Integer trangThai);
    
    @Query("SELECT pc FROM PhanCa pc WHERE pc.nhanVienId = :nhanVienId AND pc.ngayLamViec = :ngayLamViec AND pc.caId = :caId")
    Optional<PhanCa> findByNhanVienIdAndNgayLamViecAndCaId(@Param("nhanVienId") Integer nhanVienId, 
                                                           @Param("ngayLamViec") LocalDate ngayLamViec, 
                                                           @Param("caId") Integer caId);
    
    @Query("SELECT COUNT(pc) FROM PhanCa pc WHERE pc.nhanVienId = :nhanVienId AND pc.ngayLamViec = :ngayLamViec AND pc.caId = :caId")
    long countByNhanVienIdAndNgayLamViecAndCaId(@Param("nhanVienId") Integer nhanVienId, 
                                                @Param("ngayLamViec") LocalDate ngayLamViec, 
                                                @Param("caId") Integer caId);
    
    @Query("SELECT COUNT(pc) FROM PhanCa pc WHERE pc.caId = :caId AND pc.ngayLamViec = :ngayLamViec")
    long countByCaIdAndNgayLamViec(@Param("caId") Integer caId, 
                                   @Param("ngayLamViec") LocalDate ngayLamViec);
    
    @Query("SELECT pc FROM PhanCa pc WHERE pc.nhanVienId = :nhanVienId AND pc.ngayLamViec >= :ngayHienTai AND pc.trangThai = 0 ORDER BY pc.ngayLamViec, pc.ca.gioBatDau")
    List<PhanCa> findUpcomingShiftsByNhanVienId(@Param("nhanVienId") Integer nhanVienId, 
                                               @Param("ngayHienTai") LocalDate ngayHienTai);
    
    @Query("SELECT pc FROM PhanCa pc WHERE pc.nhanVienId = :nhanVienId AND pc.ngayLamViec = :ngayHienTai AND pc.trangThai = 1")
    List<PhanCa> findCurrentShiftsByNhanVienId(@Param("nhanVienId") Integer nhanVienId, 
                                              @Param("ngayHienTai") LocalDate ngayHienTai);
}
