package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.ChiTietHoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Repository
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, Integer> {
    
    List<ChiTietHoaDon> findByHoaDonId(Integer hoaDonId);
    
    List<ChiTietHoaDon> findBySanPhamId(Integer sanPhamId);
    
    @Query("SELECT c FROM ChiTietHoaDon c WHERE c.hoaDon.id = :hoaDonId")
    List<ChiTietHoaDon> findChiTietByHoaDonId(@Param("hoaDonId") Integer hoaDonId);
    
    @Query("SELECT COUNT(c) FROM ChiTietHoaDon c WHERE c.hoaDon.trangThai = 4")
    Integer sumSoLuongBySanPhamId(@Param("sanPhamId") Integer sanPhamId);
    
    void deleteByHoaDonId(Integer hoaDonId);

    // Statistics methods for ThongKeService
    @Query(value = "SELECT COUNT(DISTINCT s.id) FROM hoa_don_chi_tiet c " +
           "JOIN hoa_don h ON h.id = c.id_hoa_don " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = c.id_ctsp " +
           "JOIN san_pham s ON s.id = ctsp.id_sp " +
           "WHERE h.trang_thai IN (1, 4) AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long countDistinctSanPhamByDateRange(@Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT TOP (:limit) s.id as id, s.ten_san_pham as name, s.id_danh_muc as category, " +
           "COUNT(c.id) as quantitySold, " +
           "SUM(c.thanh_tien) as revenue, " +
           "s.ten_san_pham as image " +
           "FROM hoa_don_chi_tiet c " +
           "JOIN hoa_don h ON h.id = c.id_hoa_don " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = c.id_ctsp " +
           "JOIN san_pham s ON s.id = ctsp.id_sp " +
           "WHERE h.trang_thai IN (1, 4) AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "GROUP BY s.id, s.ten_san_pham, s.id_danh_muc " +
           "ORDER BY COUNT(c.id) DESC", nativeQuery = true)
    List<Map<String, Object>> getTopSellingProducts(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate,
                                                    @Param("limit") int limit);

    // Products chart data - daily products sold
    @Query(value = "SELECT CAST(h.ngay_tao AS DATE) as date, COUNT(c.id) as value " +
           "FROM hoa_don_chi_tiet c " +
           "JOIN hoa_don h ON h.id = c.id_hoa_don " +
           "WHERE h.trang_thai IN (1, 4) AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(h.ngay_tao AS DATE) ORDER BY CAST(h.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getProductsChartData(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Sum total quantity sold in date range
    @Query(value = "SELECT COALESCE(COUNT(c.id), 0) FROM hoa_don_chi_tiet c " +
           "JOIN hoa_don h ON h.id = c.id_hoa_don " +
           "WHERE h.trang_thai IN (1, 4) AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long sumSoLuongByDateRange(@Param("startDate") LocalDate startDate,
                               @Param("endDate") LocalDate endDate);

    // Sum quantity sold by product ID in date range
    @Query(value = "SELECT COALESCE(COUNT(c.id), 0) FROM hoa_don_chi_tiet c " +
           "JOIN hoa_don h ON h.id = c.id_hoa_don " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = c.id_ctsp " +
           "WHERE ctsp.id_sp = :productId AND h.trang_thai IN (1, 4) AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", nativeQuery = true)
    Long sumSoLuongByProductIdAndDateRange(@Param("productId") Long productId,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    // New methods for inventory statistics
    
    // Count products that are running low (quantity < 10)
    @Query(value = "SELECT COUNT(DISTINCT ctsp.id) FROM chi_tiet_san_pham ctsp " +
           "WHERE ctsp.so_luong < 10 AND ctsp.so_luong > 0", nativeQuery = true)
    Long countSanPhamSapHetHang();

    // Count products that are out of stock
    @Query(value = "SELECT COUNT(DISTINCT ctsp.id) FROM chi_tiet_san_pham ctsp " +
           "WHERE ctsp.so_luong = 0", nativeQuery = true)
    Long countSanPhamHetHang();

    // Get total inventory value
    @Query(value = "SELECT COALESCE(SUM(ctsp.so_luong * ctsp.gia_nhap), 0) " +
           "FROM chi_tiet_san_pham ctsp " +
           "WHERE ctsp.so_luong > 0", nativeQuery = true)
    BigDecimal getGiaTriTonKho();

    // Get top products by inventory
    @Query(value = "SELECT TOP (:limit) sp.ten_san_pham as productName, " +
           "ctsp.so_luong as quantity, " +
           "ctsp.gia_nhap as costPrice, " +
           "ctsp.gia_ban as sellPrice, " +
           "COALESCE(ctsp.so_luong * ctsp.gia_nhap, 0) as inventoryValue " +
           "FROM chi_tiet_san_pham ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "WHERE ctsp.so_luong > 0 " +
           "ORDER BY ctsp.so_luong DESC", nativeQuery = true)
    List<Map<String, Object>> getTopSanPhamTonKho(@Param("limit") int limit);
}














