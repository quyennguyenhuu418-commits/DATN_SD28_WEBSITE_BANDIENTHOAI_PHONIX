package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.service.DashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller xử lý các API dashboard và tổng quan hệ thống
 */
@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    /**
     * Lấy tổng quan dashboard
     */
    @GetMapping("/tong-quan")
    public ResponseEntity<Map<String, Object>> layTongQuanDashboard() {
        try {
            Map<String, Object> ketQua = dashboardService.layTongQuanDashboard();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy tổng quan dashboard: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy đơn hàng gần đây - chỉ 5 đơn hàng mới nhất
     */
    @GetMapping("/don-hang-gan-day")
    public ResponseEntity<Map<String, Object>> layDonHangGanDay() {
        try {
            Map<String, Object> ketQua = dashboardService.layDonHangGanDay();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy đơn hàng gần đây: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy sản phẩm bán chạy nhất - chỉ 3 sản phẩm top
     */
    @GetMapping("/san-pham-ban-chay-nhat")
    public ResponseEntity<Map<String, Object>> laySanPhamBanChayNhat() {
        try {
            Map<String, Object> ketQua = dashboardService.laySanPhamBanChayNhat();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy sản phẩm bán chạy: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy biểu đồ doanh thu
     */
    @GetMapping("/bieu-do-doanh-thu")
    public ResponseEntity<Map<String, Object>> layBieuDoDoanhThu() {
        try {
            Map<String, Object> ketQua = dashboardService.layBieuDoDoanhThu();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy biểu đồ doanh thu: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê nhanh
     */
    @GetMapping("/thong-ke-nhanh")
    public ResponseEntity<Map<String, Object>> layThongKeNhanh() {
        try {
            Map<String, Object> ketQua = dashboardService.layThongKeNhanh();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê nhanh: " + e.getMessage()
            ));
        }
    }
}
