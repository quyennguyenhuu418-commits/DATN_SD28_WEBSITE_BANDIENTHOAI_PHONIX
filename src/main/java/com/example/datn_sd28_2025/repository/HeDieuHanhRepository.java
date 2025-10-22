package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.HeDieuHanh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HeDieuHanhRepository extends JpaRepository<HeDieuHanh, Integer> {
    Optional<HeDieuHanh> findByMaHeDieuHanh(String maHeDieuHanh);
    
    @Query("SELECT h FROM HeDieuHanh h WHERE h.trangThai = 1")
    List<HeDieuHanh> findAllActive();
    
    @Query("SELECT h FROM HeDieuHanh h WHERE h.trangThai = 1")
    Page<HeDieuHanh> findAllActive(Pageable pageable);
    
    @Query("SELECT h FROM HeDieuHanh h WHERE h.tenHeDieuHanh LIKE %:ten%")
    List<HeDieuHanh> findByTenHeDieuHanhContaining(@Param("ten") String ten);
    
    @Query("SELECT h FROM HeDieuHanh h WHERE h.maHeDieuHanh LIKE %:ma%")
    List<HeDieuHanh> findByMaHeDieuHanhContaining(@Param("ma") String ma);
}
