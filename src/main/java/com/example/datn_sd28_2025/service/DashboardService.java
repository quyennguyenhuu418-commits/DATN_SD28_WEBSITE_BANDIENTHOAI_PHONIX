package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service xử lý logic dashboard và tổng quan hệ thống
 */
@Service
public class DashboardService {

    @Autowired
    private HoaDonRepository hoaDonRepository;
    
    @Autowired
    private SanPhamRepository sanPhamRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private DanhMucRepository danhMucRepository;
    
    @Autowired
    private HangRepository hangRepository;
    
    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    /**
     * Lấy tổng quan dashboard - thông tin cơ bản của hệ thống
     */
    public Map<String, Object> layTongQuanDashboard() {
        try {
            Map<String, Object> ketQua = new HashMap<>();
            
            // Thống kê cơ bản hệ thống
            ketQua.put("tongNhanVien", nhanVienRepository.count());
            ketQua.put("tongDanhMuc", danhMucRepository.count());
            ketQua.put("tongHang", hangRepository.count());
            ketQua.put("tongSanPham", sanPhamRepository.count());
            ketQua.put("tongKhachHang", khachHangRepository.count());
            
            // Thống kê hôm nay - chỉ những gì cần thiết
            LocalDate homNay = LocalDate.now();
            LocalDateTime batDauNgay = homNay.atStartOfDay();
            LocalDateTime ketThucNgay = homNay.atTime(23, 59, 59);
            
            // Doanh thu hôm nay
            BigDecimal doanhThuHomNay = hoaDonRepository.getTotalRevenueByDateRangeCorrected(homNay, homNay);
            ketQua.put("doanhThuHomNay", doanhThuHomNay != null ? doanhThuHomNay : BigDecimal.ZERO);
            
            // Đơn hàng hôm nay
            Long donHangHomNay = hoaDonRepository.countByNgayTaoBetweenAndTrangThai(batDauNgay, ketThucNgay, 1);
            ketQua.put("donHangHomNay", donHangHomNay != null ? donHangHomNay : 0L);
            
            // Khách hàng mới hôm nay
            Long khachHangMoiHomNay = khachHangRepository.countByNgayTaoBetween(batDauNgay, ketThucNgay);
            ketQua.put("khachHangMoiHomNay", khachHangMoiHomNay != null ? khachHangMoiHomNay : 0L);
            
            // Thời gian cập nhật
            ketQua.put("thoiGianCapNhat", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy tổng quan dashboard: " + e.getMessage());
            return taoDuLieuMacDinh();
        }
    }

    /**
     * Lấy đơn hàng gần đây - chỉ 5 đơn hàng mới nhất
     */
    public Map<String, Object> layDonHangGanDay() {
        try {
            List<Map<String, Object>> donHangGanDay = hoaDonRepository.getRecentOrders(5);
            
            Map<String, Object> ketQua = new HashMap<>();
            ketQua.put("donHangGanDay", donHangGanDay);
            ketQua.put("tongSo", donHangGanDay.size());
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy đơn hàng gần đây: " + e.getMessage());
            return Map.of("donHangGanDay", new ArrayList<>(), "tongSo", 0);
        }
    }

    /**
     * Lấy sản phẩm bán chạy nhất - chỉ 3 sản phẩm top
     */
    public Map<String, Object> laySanPhamBanChayNhat() {
        try {
            // Lấy 7 ngày gần đây
            LocalDate denNgay = LocalDate.now();
            LocalDate tuNgay = denNgay.minusDays(7);
            
            List<Map<String, Object>> sanPhamBanChay = hoaDonRepository.getTopSellingProductsByDateRange(tuNgay, denNgay, 3);
            
            Map<String, Object> ketQua = new HashMap<>();
            ketQua.put("sanPhamBanChay", sanPhamBanChay);
            ketQua.put("tongSo", sanPhamBanChay.size());
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy sản phẩm bán chạy: " + e.getMessage());
            return Map.of("sanPhamBanChay", new ArrayList<>(), "tongSo", 0);
        }
    }

    /**
     * Lấy biểu đồ doanh thu 7 ngày gần đây - chỉ cho dashboard
     */
    public Map<String, Object> layBieuDoDoanhThu() {
        try {
            LocalDate denNgay = LocalDate.now();
            LocalDate tuNgay = denNgay.minusDays(7);
            
            List<Map<String, Object>> duLieuBieuDo = hoaDonRepository.getRevenueByDateRangeCorrected(tuNgay, denNgay);
            
            Map<String, Object> ketQua = new HashMap<>();
            ketQua.put("duLieuBieuDo", duLieuBieuDo);
            ketQua.put("tongDoanhThu", tinhTongDoanhThu(duLieuBieuDo));
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy biểu đồ doanh thu: " + e.getMessage());
            return Map.of("duLieuBieuDo", new ArrayList<>(), "tongDoanhThu", BigDecimal.ZERO);
        }
    }

    /**
     * Lấy thống kê nhanh - chỉ những con số cơ bản
     */
    public Map<String, Object> layThongKeNhanh() {
        try {
            Map<String, Object> ketQua = new HashMap<>();
            
            // Tổng doanh thu tất cả thời gian
            BigDecimal tongDoanhThu = hoaDonRepository.getTotalRevenue();
            ketQua.put("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);
            
            // Tổng đơn hàng
            Long tongDonHang = hoaDonRepository.countByTrangThai(1);
            ketQua.put("tongDonHang", tongDonHang != null ? tongDonHang : 0L);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê nhanh: " + e.getMessage());
            return Map.of(
                "tongDoanhThu", BigDecimal.ZERO,
                "tongDonHang", 0L
            );
        }
    }

    /**
     * Tính tổng doanh thu từ dữ liệu biểu đồ
     */
    private BigDecimal tinhTongDoanhThu(List<Map<String, Object>> duLieuBieuDo) {
        double sum = duLieuBieuDo.stream()
            .mapToDouble(item -> {
                Object revenue = item.get("revenue");
                if (revenue instanceof Number) {
                    return ((Number) revenue).doubleValue();
                }
                return 0.0;
            })
            .sum();
        return BigDecimal.valueOf(sum);
    }

    /**
     * Tạo dữ liệu mặc định khi có lỗi
     */
    private Map<String, Object> taoDuLieuMacDinh() {
        Map<String, Object> ketQua = new HashMap<>();
        ketQua.put("tongNhanVien", 0L);
        ketQua.put("tongDanhMuc", 0L);
        ketQua.put("tongHang", 0L);
        ketQua.put("tongSanPham", 0L);
        ketQua.put("tongKhachHang", 0L);
        ketQua.put("doanhThuHomNay", BigDecimal.ZERO);
        ketQua.put("donHangHomNay", 0L);
        ketQua.put("khachHangMoiHomNay", 0L);
        ketQua.put("thoiGianCapNhat", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return ketQua;
    }
}
