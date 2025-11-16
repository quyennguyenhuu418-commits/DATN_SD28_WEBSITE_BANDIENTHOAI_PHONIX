package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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

    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0.0) FROM HoaDon h WHERE h.trangThai = 1 AND h.ngayTao BETWEEN :startDate AND :endDate")
    Double getTotalRevenueByDateRange(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startDate AND :endDate AND h.trangThai = :trangThai")
    Long countByNgayTaoBetweenAndTrangThai(@Param("startDate") LocalDateTime startDate,
                                           @Param("endDate") LocalDateTime endDate,
                                           @Param("trangThai") Integer trangThai);

    // Get recent orders for dashboard
    @Query(value = "SELECT TOP (:limit) h.id as id, h.ma_hoa_don as maHoaDon, " +
            "COALESCE(k.ho_ten, 'Khách lẻ') as customerName, " +
            "h.tong_tien as total, h.trang_thai as status, h.ngay_tao as createdAt " +
            "FROM hoa_don h " +
            "LEFT JOIN khach_hang k ON k.id = h.khach_hang_id " +
            "ORDER BY h.ngay_tao DESC", nativeQuery = true)
    List<Map<String, Object>> getRecentOrders(@Param("limit") int limit);

    // Monthly revenue data
    @Query(value = "SELECT MONTH(h.ngay_tao) as month, YEAR(h.ngay_tao) as year, " +
            "COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue " +
            "FROM hoa_don h WHERE h.trang_thai = 1 AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
            "GROUP BY YEAR(h.ngay_tao), MONTH(h.ngay_tao) ORDER BY YEAR(h.ngay_tao), MONTH(h.ngay_tao)", nativeQuery = true)
    List<Map<String, Object>> getMonthlyRevenueData(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);

    // Order status data
    @Query(value = "SELECT h.trang_thai as status, COUNT(h.id) as count " +
            "FROM hoa_don h WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
            "GROUP BY h.trang_thai", nativeQuery = true)
    List<Map<String, Object>> getOrderStatusData(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // Staff performance data
    @Query(value = "SELECT nv.id as id, nv.ho_ten as name, nv.chuc_vu as role, " +
            "COALESCE(SUM(h.tong_tien), 0) as revenue, COUNT(h.id) as orders " +
            "FROM hoa_don h " +
            "LEFT JOIN nhan_vien nv ON nv.id = h.nhan_vien_id " +
            "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
            "AND h.nhan_vien_id IS NOT NULL " +
            "GROUP BY nv.id, nv.ho_ten, nv.chuc_vu " +
            "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getStaffPerformanceData(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);

    @Query("SELECT h FROM HoaDon h")
    Page<HoaDon> findRecentOrders(Pageable pageable);

    // Statistics methods for ThongKeService
    @Query(value = "SELECT COALESCE(SUM(h.tong_tien), 0) FROM hoa_don h WHERE h.trang_thai = 1 AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", nativeQuery = true)
    BigDecimal getTotalRevenueByDateRangeLocalDate(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    // New method: Calculate revenue with correct logic for COD vs Prepaid
    @Query(value = "SELECT COALESCE(SUM(h.tong_tien_sau_giam), 0) FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4)", nativeQuery = true) // Lấy tất cả hóa đơn đã thanh toán hoặc hoàn thành
    BigDecimal getTotalRevenueByDateRangeCorrected(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0) FROM HoaDon h WHERE h.trangThai = 1")
    BigDecimal getTotalRevenue();

    @Query(value = "SELECT CAST(h.ngay_tao AS DATE) as date, COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue " +
           "FROM hoa_don h WHERE h.trang_thai = 1 AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(h.ngay_tao AS DATE) ORDER BY CAST(h.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                                    @Param("endDate") LocalDate endDate);
    
    // New method: Revenue by date with correct logic for COD vs Prepaid
    @Query(value = "SELECT CAST(h.ngay_tao AS DATE) as date, COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " + // Lấy tất cả hóa đơn đã thanh toán hoặc hoàn thành
           "GROUP BY CAST(h.ngay_tao AS DATE) ORDER BY CAST(h.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getRevenueByDateRangeCorrected(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT CAST(h.ngay_tao AS DATE) as date, COUNT(h.id) as count " +
           "FROM hoa_don h WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(h.ngay_tao AS DATE) ORDER BY CAST(h.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getOrdersByDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    // Customer chart data - new customers by date
    @Query(value = "SELECT CAST(k.ngay_tao AS DATE) as date, COUNT(k.id) as count " +
           "FROM khach_hang k WHERE CAST(k.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "GROUP BY CAST(k.ngay_tao AS DATE) ORDER BY CAST(k.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getCustomersByDateRange(@Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
    
    // Customer revenue by date - revenue from new customers
    @Query(value = "SELECT CAST(h.ngay_tao AS DATE) as date, COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue " +
           "FROM hoa_don h " +
           "JOIN khach_hang k ON k.id = h.khach_hang_id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " + // Lấy tất cả hóa đơn đã thanh toán hoặc hoàn thành
           "GROUP BY CAST(h.ngay_tao AS DATE) ORDER BY CAST(h.ngay_tao AS DATE)", nativeQuery = true)
    List<Map<String, Object>> getCustomerRevenueByDateRange(@Param("startDate") LocalDate startDate,
                                                            @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT TOP (:limit) h.id as id, h.ma_hoa_don as maHoaDon, " +
           "COALESCE(k.ho_ten, 'Khách lẻ') as customerName, " +
           "h.tong_tien_sau_giam as total, h.trang_thai as status, h.ngay_tao as createdAt " +
           "FROM hoa_don h " +
           "LEFT JOIN khach_hang k ON k.id = h.khach_hang_id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "ORDER BY h.ngay_tao DESC", nativeQuery = true)
    List<Map<String, Object>> getRecentOrders(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("limit") int limit);

    // Methods for ThongKeService
    @Query(value = "SELECT COUNT(DISTINCT h.khach_hang_id) FROM hoa_don h " +
           "WHERE h.khach_hang_id IS NOT NULL AND CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate", 
           nativeQuery = true)
    Long countDistinctCustomersByDateRange(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT TOP (:limit) " +
           "COALESCE(h.khach_hang_id, 0) as customerId, " +
           "CASE WHEN h.khach_hang_id IS NULL THEN h.ten_khach_hang ELSE k.ho_ten END as customerName, " +
           "COALESCE(SUM(h.tong_tien_sau_giam), 0) as totalSpent, " +
           "COUNT(h.id) as orderCount " +
           "FROM hoa_don h " +
           "LEFT JOIN khach_hang k ON k.id = h.khach_hang_id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY h.khach_hang_id, k.ho_ten, h.ten_khach_hang " +
           "ORDER BY totalSpent DESC", nativeQuery = true)
    List<Map<String, Object>> getVipCustomers(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("limit") int limit);

    // Debug query to check raw data
    @Query(value = "SELECT h.id, h.khach_hang_id, h.tong_tien_sau_giam, h.trang_thai, h.ngay_tao, k.ho_ten " +
           "FROM hoa_don h " +
           "LEFT JOIN khach_hang k ON k.id = h.khach_hang_id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "ORDER BY h.ngay_tao DESC", nativeQuery = true)
    List<Map<String, Object>> debugHoaDonData(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT DATEPART(hour, h.ngay_tao) as hour, COUNT(h.id) as count " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY DATEPART(hour, h.ngay_tao) " +
           "ORDER BY hour", nativeQuery = true)
    List<Map<String, Object>> getOrdersByHour(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT DATEPART(weekday, h.ngay_tao) as dayOfWeek, COUNT(h.id) as count " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY DATEPART(weekday, h.ngay_tao) " +
           "ORDER BY dayOfWeek", nativeQuery = true)
    List<Map<String, Object>> getOrdersByDayOfWeek(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    @Query(value = "SELECT COUNT(DISTINCT h1.khach_hang_id) " +
           "FROM hoa_don h1 " +
           "WHERE h1.khach_hang_id IS NOT NULL " +
           "AND CAST(h1.ngay_tao AS DATE) BETWEEN :prevStartDate AND :prevEndDate " +
           "AND EXISTS (SELECT 1 FROM hoa_don h2 " +
           "WHERE h2.khach_hang_id = h1.khach_hang_id " +
           "AND CAST(h2.ngay_tao AS DATE) BETWEEN :currentStartDate AND :currentEndDate)", 
           nativeQuery = true)
    Long countReturningCustomers(@Param("prevStartDate") LocalDate prevStartDate,
                                @Param("prevEndDate") LocalDate prevEndDate,
                                @Param("currentStartDate") LocalDate currentStartDate,
                                @Param("currentEndDate") LocalDate currentEndDate);

    // Methods for ThongKeService - Top selling products
    @Query(value = "SELECT TOP (:limit) sp.id as productId, sp.ten_san_pham as productName, " +
           "COUNT(cthd.id) as totalSold, " +
           "COALESCE(SUM(cthd.thanh_tien), 0) as totalRevenue " +
           "FROM hoa_don_chi_tiet cthd " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN hoa_don h ON h.id = cthd.id_hoa_don " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " + // Lấy tất cả hóa đơn đã thanh toán hoặc hoàn thành
           "GROUP BY sp.id, sp.ten_san_pham " +
           "ORDER BY totalSold DESC", nativeQuery = true)
    List<Map<String, Object>> getTopSellingProductsByDateRange(@Param("startDate") LocalDate startDate,
                                                                @Param("endDate") LocalDate endDate,
                                                                @Param("limit") int limit);

    // Methods for ThongKeService - Staff performance
    @Query(value = "SELECT nv.id as id, nv.ho_ten as name, nv.chuc_vu as role, " +
           "COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue, COUNT(h.id) as orders " +
           "FROM hoa_don h " +
           "LEFT JOIN nhan_vien nv ON nv.id = h.nhan_vien_id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.nhan_vien_id IS NOT NULL " +
           "AND h.trang_thai IN (1, 4) " + // Lấy tất cả hóa đơn đã thanh toán hoặc hoàn thành
           "GROUP BY nv.id, nv.ho_ten, nv.chuc_vu " +
           "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getStaffPerformanceByDateRange(@Param("startDate") LocalDate startDate,
                                                             @Param("endDate") LocalDate endDate);

    // Count by status
    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.trangThai = :trangThai")
    Long countByTrangThai(@Param("trangThai") Integer trangThai);

    // New methods for extended statistics
    
    // Revenue by brand
    @Query(value = "SELECT ha.ten as brandName, COALESCE(SUM(hd.tong_tien_sau_giam), 0) as revenue " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN hang ha ON ha.id = sp.id_hang " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY ha.ten " +
           "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByBrand(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate);

    // Quantity by brand
    @Query(value = "SELECT ha.ten as brandName, COUNT(cthd.id) as quantity " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN hang ha ON ha.id = sp.id_hang " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY ha.ten " +
           "ORDER BY quantity DESC", nativeQuery = true)
    List<Map<String, Object>> getQuantityByBrand(@Param("startDate") LocalDate startDate,
                                                 @Param("endDate") LocalDate endDate);

    // Top brands
    @Query(value = "SELECT TOP (:limit) ha.ten as brandName, " +
           "COALESCE(SUM(hd.tong_tien_sau_giam), 0) as revenue, " +
           "COUNT(cthd.id) as quantity " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN hang ha ON ha.id = sp.id_hang " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY ha.ten " +
           "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getTopBrands(@Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate,
                                           @Param("limit") int limit);

    // Revenue by category
    @Query(value = "SELECT dm.ten_danh_muc as categoryName, COALESCE(SUM(hd.tong_tien_sau_giam), 0) as revenue " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN danh_muc dm ON dm.id = sp.id_danh_muc " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY dm.ten_danh_muc " +
           "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getRevenueByCategory(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    // Quantity by category
    @Query(value = "SELECT dm.ten_danh_muc as categoryName, COUNT(cthd.id) as quantity " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN danh_muc dm ON dm.id = sp.id_danh_muc " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY dm.ten_danh_muc " +
           "ORDER BY quantity DESC", nativeQuery = true)
    List<Map<String, Object>> getQuantityByCategory(@Param("startDate") LocalDate startDate,
                                                     @Param("endDate") LocalDate endDate);

    // Top categories
    @Query(value = "SELECT TOP (:limit) dm.ten_danh_muc as categoryName, " +
           "COALESCE(SUM(hd.tong_tien_sau_giam), 0) as revenue, " +
           "COUNT(cthd.id) as quantity " +
           "FROM hoa_don hd " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = hd.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "JOIN san_pham sp ON sp.id = ctsp.id_sp " +
           "JOIN danh_muc dm ON dm.id = sp.id_danh_muc " +
           "WHERE CAST(hd.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND hd.trang_thai IN (1, 4) " +
           "GROUP BY dm.ten_danh_muc " +
           "ORDER BY revenue DESC", nativeQuery = true)
    List<Map<String, Object>> getTopCategories(@Param("startDate") LocalDate startDate,
                                                @Param("endDate") LocalDate endDate,
                                                @Param("limit") int limit);

    // Trend by hour
    @Query(value = "SELECT DATEPART(hour, h.ngay_tao) as hour, " +
           "COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue, " +
           "COUNT(h.id) as orders " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY DATEPART(hour, h.ngay_tao) " +
           "ORDER BY hour", nativeQuery = true)
    List<Map<String, Object>> getTrendByHour(@Param("startDate") LocalDate startDate,
                                             @Param("endDate") LocalDate endDate);

    // Trend by day of week
    @Query(value = "SELECT DATEPART(weekday, h.ngay_tao) as dayOfWeek, " +
           "COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue, " +
           "COUNT(h.id) as orders " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY DATEPART(weekday, h.ngay_tao) " +
           "ORDER BY dayOfWeek", nativeQuery = true)
    List<Map<String, Object>> getTrendByDayOfWeek(@Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);

    // Trend by month
    @Query(value = "SELECT MONTH(h.ngay_tao) as month, YEAR(h.ngay_tao) as year, " +
           "COALESCE(SUM(h.tong_tien_sau_giam), 0) as revenue, " +
           "COUNT(h.id) as orders " +
           "FROM hoa_don h " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4) " +
           "GROUP BY YEAR(h.ngay_tao), MONTH(h.ngay_tao) " +
           "ORDER BY year, month", nativeQuery = true)
    List<Map<String, Object>> getTrendByMonth(@Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate);

    // Total cost by date range
    @Query(value = "SELECT COALESCE(SUM(cthd.don_gia * ISNULL(imei_count.count, 1)), 0) " +
           "FROM hoa_don h " +
           "JOIN hoa_don_chi_tiet cthd ON cthd.id_hoa_don = h.id " +
           "JOIN chi_tiet_san_pham ctsp ON ctsp.id = cthd.id_ctsp " +
           "LEFT JOIN (SELECT id_hoa_don_chi_tiet, COUNT(*) as count FROM imei_da_ban GROUP BY id_hoa_don_chi_tiet) imei_count " +
           "ON imei_count.id_hoa_don_chi_tiet = cthd.id " +
           "WHERE CAST(h.ngay_tao AS DATE) BETWEEN :startDate AND :endDate " +
           "AND h.trang_thai IN (1, 4)", nativeQuery = true)
    BigDecimal getTotalCostByDateRange(@Param("startDate") LocalDate startDate,
                                      @Param("endDate") LocalDate endDate);

    // Methods for GiaoCaService - Calculate revenue for a shift
    @Query("SELECT COALESCE(SUM(h.tongTienSauGiam), 0) FROM HoaDon h WHERE h.ngayTao BETWEEN :startTime AND :endTime AND h.trangThai IN (1, 4)")
    BigDecimal getTotalRevenueByTimeRange(@Param("startTime") LocalDateTime startTime,
                                         @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startTime AND :endTime AND h.trangThai IN (1, 4)")
    Long countOrdersByTimeRange(@Param("startTime") LocalDateTime startTime,
                               @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startTime AND :endTime AND h.trangThai IN (1, 4) AND EXISTS (SELECT ctt FROM ChiTietThanhToan ctt WHERE ctt.hoaDon.id = h.id AND ctt.phuongThucThanhToan.tenPhuongThuc LIKE '%Tiền mặt%')")
    Long countCashOrdersByTimeRange(@Param("startTime") LocalDateTime startTime,
                                   @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(h) FROM HoaDon h WHERE h.ngayTao BETWEEN :startTime AND :endTime AND h.trangThai IN (1, 4) AND EXISTS (SELECT ctt FROM ChiTietThanhToan ctt WHERE ctt.hoaDon.id = h.id AND (ctt.phuongThucThanhToan.tenPhuongThuc LIKE '%Chuyển khoản%' OR ctt.phuongThucThanhToan.tenPhuongThuc LIKE '%VNPAY%' OR ctt.phuongThucThanhToan.tenPhuongThuc LIKE '%ZaloPay%'))")
    Long countBankTransferOrdersByTimeRange(@Param("startTime") LocalDateTime startTime,
                                           @Param("endTime") LocalDateTime endTime);

}