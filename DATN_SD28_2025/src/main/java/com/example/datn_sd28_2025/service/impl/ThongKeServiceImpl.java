package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.repository.*;
import com.example.datn_sd28_2025.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ThongKeServiceImpl implements ThongKeService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Override
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        // Tổng sản phẩm (chỉ đếm sản phẩm có số lượng > 0)
        long totalProducts = chiTietSanPhamRepository.countBySoLuongGreaterThan(0);
        stats.put("totalProducts", totalProducts);

        // Đơn hàng hôm nay
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        long ordersToday = hoaDonRepository.countByNgayTaoBetweenAndTrangThai(startOfDay, endOfDay, 1);
        stats.put("ordersToday", ordersToday);

        // Doanh thu hôm nay
        Double revenueToday = hoaDonRepository.getTotalRevenueByDateRange(startOfDay, endOfDay);
        stats.put("revenueToday", revenueToday != null ? revenueToday : 0.0);

        // Khách hàng mới hôm nay
        long newCustomersToday = khachHangRepository.countByNgayTaoBetween(startOfDay, endOfDay);
        stats.put("newCustomersToday", newCustomersToday);

        // Tính phần trăm thay đổi (so với hôm qua)
        LocalDate yesterday = today.minusDays(1);
        LocalDateTime startOfYesterday = yesterday.atStartOfDay();
        LocalDateTime endOfYesterday = yesterday.atTime(23, 59, 59);

        // Phần trăm đơn hàng
        long ordersYesterday = hoaDonRepository.countByNgayTaoBetweenAndTrangThai(startOfYesterday, endOfYesterday, 1);
        double ordersChangePercent = calculatePercentageChange(ordersYesterday, ordersToday);
        stats.put("ordersChangePercent", ordersChangePercent);

        // Phần trăm doanh thu
        Double revenueYesterday = hoaDonRepository.getTotalRevenueByDateRange(startOfYesterday, endOfYesterday);
        double revenueYesterdayValue = revenueYesterday != null ? revenueYesterday : 0.0;
        double revenueChangePercent = calculatePercentageChange(revenueYesterdayValue, revenueToday != null ? revenueToday : 0.0);
        stats.put("revenueChangePercent", revenueChangePercent);

        // Phần trăm khách hàng mới
        long newCustomersYesterday = khachHangRepository.countByNgayTaoBetween(startOfYesterday, endOfYesterday);
        double customersChangePercent = calculatePercentageChange(newCustomersYesterday, newCustomersToday);
        stats.put("customersChangePercent", customersChangePercent);

        // Phần trăm sản phẩm (so với tuần trước)
        LocalDate weekAgo = today.minusWeeks(1);
        LocalDateTime startOfWeekAgo = weekAgo.atStartOfDay();
        LocalDateTime endOfWeekAgo = weekAgo.atTime(23, 59, 59);
        long productsWeekAgo = chiTietSanPhamRepository.countBySoLuongGreaterThan(0);
        double productsChangePercent = calculatePercentageChange(productsWeekAgo, totalProducts);
        stats.put("productsChangePercent", productsChangePercent);

        return stats;
    }

    @Override
    public Map<String, Object> getRecentOrders(int page, int size) {
        Map<String, Object> result = new HashMap<>();

        // Lấy đơn hàng gần đây
        Pageable pageable = PageRequest.of(page, size, Sort.by("ngayTao").descending());
        Page<HoaDon> ordersPage = hoaDonRepository.findRecentOrders(pageable);

        List<Map<String, Object>> orders = ordersPage.getContent().stream()
            .map(order -> {
                Map<String, Object> orderMap = new HashMap<>();
                orderMap.put("id", order.getId());
                orderMap.put("maHoaDon", order.getMaHoaDon());
                orderMap.put("tenKhachHang", order.getTenKhachHang() != null ? order.getTenKhachHang() : "Khách lẻ");
                orderMap.put("soDienThoai", order.getSoDienThoai());
                orderMap.put("tongTien", order.getTongTien());
                orderMap.put("trangThai", order.getTrangThai());
                orderMap.put("ngayTao", order.getNgayTao());
                orderMap.put("soLuongSanPham", 0); // Will be updated separately if needed
                
                // Format trạng thái
                String trangThaiText = getTrangThaiText(order.getTrangThai());
                orderMap.put("trangThaiText", trangThaiText);
                
                return orderMap;
            })
            .toList();

        result.put("orders", orders);
        result.put("totalElements", ordersPage.getTotalElements());
        result.put("totalPages", ordersPage.getTotalPages());
        result.put("currentPage", ordersPage.getNumber());
        result.put("size", ordersPage.getSize());

        return result;
    }

    private double calculatePercentageChange(double oldValue, double newValue) {
        if (oldValue == 0) {
            return newValue > 0 ? 100.0 : 0.0;
        }
        return Math.round(((newValue - oldValue) / oldValue) * 100.0 * 100.0) / 100.0;
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        
        return switch (trangThai) {
            case 0 -> "Đã hủy";
            case 1 -> "Chờ xử lý";
            case 2 -> "Đang xử lý";
            case 3 -> "Đã hoàn thành";
            case 4 -> "Đã giao hàng";
            default -> "Không xác định";
        };
    }
}
