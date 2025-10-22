package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {
    
    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai = 1")
    List<KhuyenMai> findAllActive();
    
    @Query("SELECT k FROM KhuyenMai k WHERE k.maKhuyenMai = :maKhuyenMai")
    KhuyenMai findByMaKhuyenMai(@Param("maKhuyenMai") String maKhuyenMai);
    
    @Query("SELECT k FROM KhuyenMai k WHERE k.trangThai = 1 AND k.ngayBatDau <= :now AND k.ngayKetThuc >= :now")
    List<KhuyenMai> findActivePromotions(@Param("now") LocalDateTime now);
    
    @Query("SELECT k FROM KhuyenMai k WHERE k.tenKhuyenMai LIKE %:ten%")
    List<KhuyenMai> findByTenKhuyenMaiContaining(@Param("ten") String ten);
}
