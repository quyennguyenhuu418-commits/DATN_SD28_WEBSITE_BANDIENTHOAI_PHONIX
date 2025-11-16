package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.service.ThongKeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Controller xử lý các API thống kê và báo cáo
 */
@RestController
@RequestMapping("/api/thong-ke")
@CrossOrigin(origins = "*")
public class ThongKeController {

    @Autowired
    private ThongKeService thongKeService;

    /**
     * Lấy thống kê chi tiết đầy đủ theo khoảng thời gian
     */
    @GetMapping("/chi-tiet")
    public ResponseEntity<Map<String, Object>> layThongKeChiTiet(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeChiTiet(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê chi tiết: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê doanh thu theo ngày
     */
    @GetMapping("/doanh-thu-theo-ngay")
    public ResponseEntity<Map<String, Object>> layThongKeDoanhThuTheoNgay(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeDoanhThuTheoNgay(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê doanh thu: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê sản phẩm bán chạy
     */
    @GetMapping("/san-pham-ban-chay")
    public ResponseEntity<Map<String, Object>> layThongKeSanPhamBanChay(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeSanPhamBanChay(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê sản phẩm bán chạy: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê nhân viên
     */
    @GetMapping("/nhan-vien")
    public ResponseEntity<Map<String, Object>> layThongKeNhanVien(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeNhanVien(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê nhân viên: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê tồn kho
     */
    @GetMapping("/ton-kho")
    public ResponseEntity<Map<String, Object>> layThongKeTonKho() {
        try {
            Map<String, Object> ketQua = thongKeService.layThongKeTonKho();
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê tồn kho: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê theo thương hiệu
     */
    @GetMapping("/thuong-hieu")
    public ResponseEntity<Map<String, Object>> layThongKeThuongHieu(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeThuongHieu(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê thương hiệu: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê theo danh mục
     */
    @GetMapping("/danh-muc")
    public ResponseEntity<Map<String, Object>> layThongKeDanhMuc(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeDanhMuc(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê danh mục: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê xu hướng mua hàng
     */
    @GetMapping("/xu-huong")
    public ResponseEntity<Map<String, Object>> layThongKeXuHuong(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeXuHuong(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê xu hướng: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy thống kê tài chính
     */
    @GetMapping("/tai-chinh")
    public ResponseEntity<Map<String, Object>> layThongKeTaiChinh(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay) {
        try {
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            Map<String, Object> ketQua = thongKeService.layThongKeTaiChinh(tuNgayDate, denNgayDate);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy thống kê tài chính: " + e.getMessage()
            ));
        }
    }

    /**
     * Lấy khách hàng chi nhiều nhất (VIP customers)
     */
    @GetMapping("/khach-hang-vip")
    public ResponseEntity<Map<String, Object>> layKhachHangVIP(
            @RequestParam(required = false) String tuNgay,
            @RequestParam(required = false) String denNgay,
            @RequestParam(defaultValue = "10") int limit) {
        try {
            System.out.println("=== VIP CUSTOMERS API CALLED ===");
            System.out.println("tuNgay: " + tuNgay);
            System.out.println("denNgay: " + denNgay);
            System.out.println("limit: " + limit);
            
            LocalDate tuNgayDate = tuNgay != null ? LocalDate.parse(tuNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now().minusDays(30);
            LocalDate denNgayDate = denNgay != null ? LocalDate.parse(denNgay, DateTimeFormatter.ofPattern("yyyy-MM-dd")) : LocalDate.now();
            
            System.out.println("Parsed dates: " + tuNgayDate + " to " + denNgayDate);
            
            Map<String, Object> ketQua = thongKeService.layKhachHangVIP(tuNgayDate, denNgayDate, limit);
            
            return ResponseEntity.ok(Map.of(
                "thanhCong", true,
                "duLieu", ketQua
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "thanhCong", false,
                "loi", "Lỗi khi lấy khách hàng VIP: " + e.getMessage()
            ));
        }
    }
}
