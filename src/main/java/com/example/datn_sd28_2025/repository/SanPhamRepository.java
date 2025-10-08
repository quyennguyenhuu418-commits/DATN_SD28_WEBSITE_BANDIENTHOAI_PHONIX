package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.SanPham;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    Optional<SanPham> findByMaSanPham(String maSanPham);
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.trangThai = 1 ORDER BY s.ngayTao DESC")
    List<SanPham> findAllActive();
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.danhMuc.id = :danhMucId AND s.trangThai = 1 ORDER BY s.ngayTao DESC")
    List<SanPham> findByDanhMucIdAndTrangThaiTrue(@Param("danhMucId") Integer danhMucId);
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.hang.id = :hangId AND s.trangThai = 1 ORDER BY s.ngayTao DESC")
    List<SanPham> findByHangIdAndTrangThaiTrue(@Param("hangId") Integer hangId);
    
    List<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham);
    
    @Query("SELECT s FROM SanPham s LEFT JOIN FETCH s.danhMuc LEFT JOIN FETCH s.hang WHERE s.tenSanPham LIKE %:tenSanPham% AND s.trangThai = 1 ORDER BY s.ngayTao DESC")
    List<SanPham> findByTenSanPhamContainingIgnoreCaseAndTrangThaiTrue(@Param("tenSanPham") String tenSanPham);
}
