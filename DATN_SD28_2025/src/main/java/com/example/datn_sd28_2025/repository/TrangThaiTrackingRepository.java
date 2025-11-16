package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.TrangThaiTracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrangThaiTrackingRepository extends JpaRepository<TrangThaiTracking, Integer> {
    
    @Query("SELECT t FROM TrangThaiTracking t WHERE t.hoaDonId = :hoaDonId ORDER BY t.thoiGian ASC")
    List<TrangThaiTracking> findByHoaDonIdOrderByThoiGianAsc(@Param("hoaDonId") Integer hoaDonId);
    
    @Query("SELECT t FROM TrangThaiTracking t WHERE t.hoaDonId = :hoaDonId AND t.trangThai = :trangThai ORDER BY t.thoiGian DESC")
    List<TrangThaiTracking> findByHoaDonIdAndTrangThaiOrderByThoiGianDesc(@Param("hoaDonId") Integer hoaDonId, @Param("trangThai") Integer trangThai);
}







