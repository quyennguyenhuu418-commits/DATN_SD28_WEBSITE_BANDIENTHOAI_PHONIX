package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.PhieuGiamGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuGiamGiaRepository extends JpaRepository<PhieuGiamGia, Integer> {
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 1")
    List<PhieuGiamGia> findAllActive();
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.maPhieuGiamGia = :code")
    Optional<PhieuGiamGia> findByCode(@Param("code") String code);
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.trangThai = 1 AND p.ngayBatDau <= :currentDate AND p.ngayKetThuc >= :currentDate AND p.soLuongDung > 0")
    List<PhieuGiamGia> findValidVouchers(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.tenPhieuGiamGia LIKE %:name%")
    List<PhieuGiamGia> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.loaiPhieuGiamGia = :type")
    List<PhieuGiamGia> findByType(@Param("type") String type);
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.ngayKetThuc < :currentDate AND p.trangThai = 1")
    List<PhieuGiamGia> findExpiredVouchers(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT p FROM PhieuGiamGia p WHERE p.ngayBatDau > :currentDate AND p.trangThai = 1")
    List<PhieuGiamGia> findUpcomingVouchers(@Param("currentDate") LocalDate currentDate);
}

