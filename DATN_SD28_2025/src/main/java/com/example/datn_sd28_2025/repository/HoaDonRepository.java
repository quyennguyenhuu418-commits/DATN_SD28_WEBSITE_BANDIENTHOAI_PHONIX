package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    
    Optional<HoaDon> findByMaHoaDon(String maHoaDon);
    
    List<HoaDon> findByKhachHangId(Integer khachHangId);
    
    List<HoaDon> findByTrangThai(Integer trangThai);
    
    List<HoaDon> findByLoaiHoaDon(String loaiHoaDon);
    
    @Query("SELECT h FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    List<HoaDon> findByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT h FROM HoaDon h WHERE h.trangThai = :trangThai AND h.loaiHoaDon = :loaiHoaDon")
    List<HoaDon> findByTrangThaiAndLoaiHoaDon(@Param("trangThai") Integer trangThai, 
                                              @Param("loaiHoaDon") String loaiHoaDon);
    
    @Query("SELECT h FROM HoaDon h WHERE h.maHoaDon LIKE %:keyword% OR " +
           "(h.khachHangId IS NOT NULL AND EXISTS (SELECT k FROM KhachHang k WHERE k.id = h.khachHangId AND k.hoTen LIKE %:keyword%))")
    Page<HoaDon> searchHoaDon(@Param("keyword") String keyword, Pageable pageable);
    
    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate")
    Long countByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT SUM(h.tongTien) FROM HoaDon h WHERE h.trangThai = 1 AND h.ngayTao BETWEEN :startDate AND :endDate")
    Double sumTongTienByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, 
                                       @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COALESCE(SUM(h.tongTien), 0.0) FROM HoaDon h WHERE h.trangThai = 1 AND h.ngayTao BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate, 
                                     @Param("endDate") LocalDateTime endDate);
    
    // Statistics methods
    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate AND h.trangThai = :trangThai")
    Long countByNgayTaoBetweenAndTrangThai(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate,
                                          @Param("trangThai") Integer trangThai);
    
    @Query("SELECT h FROM HoaDon h ORDER BY h.ngayTao DESC")
    Page<HoaDon> findRecentOrders(Pageable pageable);
}