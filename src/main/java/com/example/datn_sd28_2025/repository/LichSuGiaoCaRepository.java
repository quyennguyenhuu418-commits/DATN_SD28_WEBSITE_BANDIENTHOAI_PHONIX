package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.LichSuGiaoCa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LichSuGiaoCaRepository extends JpaRepository<LichSuGiaoCa, Integer> {
    
    List<LichSuGiaoCa> findByGiaoCaId(Integer giaoCaId);
    
    List<LichSuGiaoCa> findByNguoiThucHienId(Integer nguoiThucHienId);
    
    List<LichSuGiaoCa> findByHanhDong(String hanhDong);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.giaoCaId = :giaoCaId ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByGiaoCaIdOrderByThoiGianDesc(@Param("giaoCaId") Integer giaoCaId);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.nguoiThucHienId = :nguoiThucHienId ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByNguoiThucHienIdOrderByThoiGianDesc(@Param("nguoiThucHienId") Integer nguoiThucHienId);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.thoiGian BETWEEN :startDate AND :endDate ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.giaoCaId = :giaoCaId AND lsgc.hanhDong = :hanhDong ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByGiaoCaIdAndHanhDong(@Param("giaoCaId") Integer giaoCaId, 
                                                  @Param("hanhDong") String hanhDong);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.nguoiThucHienId = :nguoiThucHienId AND lsgc.thoiGian BETWEEN :startDate AND :endDate ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByNguoiThucHienIdAndDateRange(@Param("nguoiThucHienId") Integer nguoiThucHienId, 
                                                         @Param("startDate") LocalDateTime startDate, 
                                                         @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.giaoCa.phanCa.nhanVienId = :nhanVienId ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByNhanVienId(@Param("nhanVienId") Integer nhanVienId);
    
    @Query("SELECT lsgc FROM LichSuGiaoCa lsgc WHERE lsgc.giaoCa.phanCa.caId = :caId ORDER BY lsgc.thoiGian DESC")
    List<LichSuGiaoCa> findByCaId(@Param("caId") Integer caId);
}

























