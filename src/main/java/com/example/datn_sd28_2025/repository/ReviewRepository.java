package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Integer> {
    
    @Query("SELECT r FROM Review r WHERE r.sanPham.id = :idSanPham ORDER BY r.ngayTao DESC")
    List<Review> findBySanPhamIdOrderByNgayTaoDesc(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT r FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai ORDER BY r.ngayTao DESC")
    List<Review> findBySanPhamIdAndTrangThaiOrderByNgayTaoDesc(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sanPham.id = :idSanPham")
    Double getAverageRatingBySanPhamId(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai")
    Double getAverageRatingBySanPhamIdAndTrangThai(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sanPham.id = :idSanPham")
    Long countBySanPhamId(@Param("idSanPham") Integer idSanPham);
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.sanPham.id = :idSanPham AND r.trangThai = :trangThai")
    Long countBySanPhamIdAndTrangThai(@Param("idSanPham") Integer idSanPham, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT r FROM Review r WHERE r.trangThai = :trangThai ORDER BY r.ngayTao DESC")
    List<Review> findByTrangThaiOrderByNgayTaoDesc(@Param("trangThai") Integer trangThai);
    
    @Query("SELECT r FROM Review r ORDER BY r.ngayTao DESC")
    List<Review> findAllByOrderByNgayTaoDesc();
}
