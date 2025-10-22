package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = 1")
    List<KhachHang> findAllActive();
    
    @Query("SELECT k FROM KhachHang k WHERE k.maKhachHang = :maKhachHang AND k.trangThai = 1")
    Optional<KhachHang> findByMaKhachHang(@Param("maKhachHang") String maKhachHang);
    
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai = :soDienThoai AND k.trangThai = 1 ORDER BY k.id DESC")
    List<KhachHang> findBySoDienThoai(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.email = :email AND k.trangThai = 1 ORDER BY k.id DESC")
    List<KhachHang> findByEmail(@Param("email") String email);
    
    @Query("SELECT k FROM KhachHang k WHERE k.email = :email AND k.trangThai = 1")
    Optional<KhachHang> findByEmailAndTrangThai(@Param("email") String email, @Param("trangThai") Integer trangThai);
    
    
    @Query("SELECT k FROM KhachHang k WHERE k.hoTen LIKE %:hoTen%")
    List<KhachHang> findByHoTenContaining(@Param("hoTen") String hoTen);
    
    @Query("SELECT k FROM KhachHang k WHERE k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> findBySoDienThoaiContaining(@Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.hoTen LIKE %:hoTen% OR k.soDienThoai LIKE %:soDienThoai%")
    List<KhachHang> findByHoTenContainingOrSoDienThoaiContaining(@Param("hoTen") String hoTen, @Param("soDienThoai") String soDienThoai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = 1 AND (k.hoTen LIKE %:query% OR k.soDienThoai LIKE %:query% OR k.email LIKE %:query%)")
    List<KhachHang> findByQuery(@Param("query") String query);
    
    @Query("SELECT k FROM KhachHang k WHERE k.trangThai = :trangThai")
    List<KhachHang> findByTrangThai(@Param("trangThai") Integer trangThai);
    
    @Query("SELECT k FROM KhachHang k WHERE k.id = :id AND k.trangThai = :trangThai")
    Optional<KhachHang> findByIdAndTrangThai(@Param("id") Integer id, @Param("trangThai") Integer trangThai);
    
    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.trangThai = 1")
    Long countActiveCustomers();
    
    // Statistics methods
    @Query("SELECT COUNT(k) FROM KhachHang k WHERE k.ngayTao BETWEEN :startDate AND :endDate")
    Long countByNgayTaoBetween(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate);
    
    @Query(value = "SELECT COUNT(k.id) FROM khach_hang k WHERE CAST(k.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countByNgayTaoBetweenLocalDate(@Param("startDate") LocalDate startDate, 
                                       @Param("endDate") LocalDate endDate);
    
    // Method để tìm số thứ tự lớn nhất theo prefix
    @Query("SELECT MAX(CAST(SUBSTRING(k.maKhachHang, :prefixLength + 1) AS long)) FROM KhachHang k WHERE k.maKhachHang LIKE :prefix%")
    Long findMaxSequenceByPrefix(@Param("prefix") String prefix, @Param("prefixLength") int prefixLength);
    
    // Simple status update without validation
    @Modifying
    @Query("UPDATE KhachHang k SET k.trangThai = :trangThai, k.ngayCapNhat = :ngayCapNhat WHERE k.id = :id")
    int updateStatusOnly(@Param("id") Integer id, @Param("trangThai") Integer trangThai, @Param("ngayCapNhat") LocalDateTime ngayCapNhat);
}
