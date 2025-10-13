package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = 1")
    List<KhachHang> findAllActive();
    
    @Query("SELECT k FROM KhachHang k WHERE k.maKhachHang = :maKhachHang")
    Optional<KhachHang> findByMaKhachHang(@Param("maKhachHang") String maKhachHang);
    
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai = :soDienThoai")
    Optional<KhachHang> findBySoDienThoai(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.email = :email")
    Optional<KhachHang> findByEmail(@Param("email") String email);
    
    @Query("SELECT k FROM KhachHang k WHERE k.taiKhoan = :taiKhoan")
    Optional<KhachHang> findByTaiKhoan(@Param("taiKhoan") String taiKhoan);
    
    @Query("SELECT k FROM KhachHang k WHERE k.hoTen LIKE %:hoTen%")
    List<KhachHang> findByHoTenContaining(@Param("hoTen") String hoTen);
    
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> findBySoDienThoaiContaining(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.hoTen LIKE %:hoTen% OR k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> findByHoTenContainingOrSoDienThoaiContaining(@Param("hoTen") String hoTen, @Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.hoTen LIKE %:query% OR k.soDienThoai LIKE %:query% OR k.email LIKE %:query%")
    List<KhachHang> findByQuery(@Param("query") String query);
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = :trangThai")
    List<KhachHang> findByTrangThai(@Param("trangThai") Integer trangThai);
    
    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.trangThai = 1")
    Long countActiveCustomers();
    
    // Statistics methods
    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.ngayTao BETWEEN :startDate AND :endDate")
    Long countByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
}
