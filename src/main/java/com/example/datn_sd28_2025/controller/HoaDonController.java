package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.HoaDonTrackingDTO;
import com.example.datn_sd28_2025.dto.HoaDonSearchRequestDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.entity.HoaDon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @GetMapping
    public ResponseEntity<List<HoaDonDTO>> getAll() {
        List<HoaDonDTO> hoaDons = hoaDonService.getAll();
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaDonDTO> getById(@PathVariable Integer id) {
        HoaDonDTO hoaDon = hoaDonService.getById(id);
        return hoaDon != null ? ResponseEntity.ok(hoaDon) : ResponseEntity.notFound().build();
    }

    @GetMapping("/ma-hoa-don/{maHoaDon}")
    public ResponseEntity<HoaDonDTO> getByMaHoaDon(@PathVariable String maHoaDon) {
        HoaDonDTO hoaDon = hoaDonService.getByMaHoaDon(maHoaDon);
        return hoaDon != null ? ResponseEntity.ok(hoaDon) : ResponseEntity.notFound().build();
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<HoaDonDTO>> getByKhachHangId(@PathVariable Integer khachHangId) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByKhachHangId(khachHangId);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<HoaDonDTO>> getByTrangThai(@PathVariable Integer trangThai) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByTrangThai(trangThai);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/loai-hoa-don/{loaiHoaDon}")
    public ResponseEntity<List<HoaDonDTO>> getByLoaiHoaDon(@PathVariable String loaiHoaDon) {
        List<HoaDonDTO> hoaDons = hoaDonService.getByLoaiHoaDon(loaiHoaDon);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<HoaDonDTO>> searchHoaDon(@RequestParam String keyword, Pageable pageable) {
        Page<HoaDonDTO> hoaDons = hoaDonService.searchHoaDon(keyword, pageable);
        return ResponseEntity.ok(hoaDons);
    }

    @PostMapping("/search-advanced")
    public ResponseEntity<Page<HoaDonDTO>> searchHoaDonAdvanced(@RequestBody HoaDonSearchRequestDTO searchRequest) {
        Page<HoaDonDTO> hoaDons = hoaDonService.searchHoaDonAdvanced(searchRequest);
        return ResponseEntity.ok(hoaDons);
    }

    @GetMapping("/tracking/{maHoaDon}")
    public ResponseEntity<?> getTrackingInfo(@PathVariable String maHoaDon) {
        try {
            HoaDonTrackingDTO trackingInfo = hoaDonService.getTrackingInfo(maHoaDon);
            if (trackingInfo != null) {
                return ResponseEntity.ok(trackingInfo);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi lấy thông tin tracking",
                    "message", e.getMessage(),
                    "maHoaDon", maHoaDon
            ));
        }
    }

    // Check order by phone and order code (both required)
    @GetMapping("/check")
    public ResponseEntity<?> checkOrderByPhoneAndCode(@RequestParam String phone, @RequestParam String code) {
        try {
            // Simple normalization: trim and keep digits for phone
            String normalizedPhone = phone == null ? null : phone.replaceAll("[^0-9]", "");
            if (normalizedPhone == null || normalizedPhone.isBlank() || code == null || code.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Thiếu số điện thoại hoặc mã đơn hàng",
                        "phone", phone,
                        "code", code
                ));
            }

            HoaDonDTO hoaDon = hoaDonService.getByMaHoaDon(code.trim());
            if (hoaDon == null) {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "message", "Không tìm thấy đơn hàng với mã: " + code
                ));
            }

            String orderPhone = hoaDon.getSoDienThoai() != null ? hoaDon.getSoDienThoai().replaceAll("[^0-9]", "") : "";
            boolean phoneMatch = orderPhone.endsWith(normalizedPhone) || normalizedPhone.endsWith(orderPhone) || orderPhone.equals(normalizedPhone);

            if (!phoneMatch) {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "message", "Số điện thoại không khớp với đơn hàng"
                ));
            }

            // Return minimal tracking info
            Map<String, Object> result = Map.of(
                    "found", true,
                    "maHoaDon", hoaDon.getMaHoaDon(),
                    "tenKhachHang", hoaDon.getTenKhachHang(),
                    "soDienThoai", hoaDon.getSoDienThoai(),
                    "trangThai", hoaDon.getTrangThai(),
                    "tenTrangThai", com.example.datn_sd28_2025.util.OrderStatusUtil.getStatusName(hoaDon.getTrangThai()),
                    "tongTien", hoaDon.getTongTienSauGiam()
            );

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi kiểm tra đơn hàng",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}/products")
    public ResponseEntity<?> getHoaDonProducts(@PathVariable Integer id) {
        try {
            List<Map<String, Object>> products = hoaDonService.getHoaDonProducts(id);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi lấy danh sách sản phẩm",
                    "message", e.getMessage(),
                    "hoaDonId", id
            ));
        }
    }

    @PostMapping
    public ResponseEntity<HoaDonDTO> create(@RequestBody HoaDonDTO hoaDonDTO) {
        try {
            // For simple creation without POS order request
            HoaDonDTO created = hoaDonService.updateHoaDon(0, hoaDonDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> updateHoaDon(@PathVariable Integer id, @RequestBody HoaDonDTO hoaDonDTO) {
        try {
            HoaDonDTO updated = hoaDonService.updateHoaDon(id, hoaDonDTO);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<HoaDonDTO> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer newStatus = request.get("trangThai");
            HoaDonDTO updated = hoaDonService.updateTrangThai(id, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/update-status/{maHoaDon}")
    public ResponseEntity<HoaDonDTO> updateStatusByMaHoaDon(@PathVariable String maHoaDon, @RequestBody Map<String, Integer> request) {
        try {
            Integer newStatus = request.get("trangThai");
            HoaDonDTO updated = hoaDonService.updateTrangThaiByMaHoaDon(maHoaDon, newStatus);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PostMapping("/pos-order")
    public ResponseEntity<?> createPosOrder(@RequestBody PosOrderRequest request) {
        try {
            HoaDonDTO created = hoaDonService.createOrder(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            e.printStackTrace(); // Log the error for debugging
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Có lỗi xảy ra khi tạo hóa đơn",
                    "message", e.getMessage(),
                    "details", e.getClass().getSimpleName()
            ));
        }
    }


    @PutMapping("/{id}/trang-thai")
    public ResponseEntity<HoaDonDTO> updateTrangThai(@PathVariable Integer id, @RequestParam Integer trangThai) {
        try {
            HoaDonDTO updated = hoaDonService.updateTrangThai(id, trangThai);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            hoaDonService.deleteHoaDon(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/statistics/count")
    public ResponseEntity<Long> countByDateRange(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        Long count = hoaDonService.countByDateRange(startDate, endDate);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/statistics/revenue")
    public ResponseEntity<Double> getTotalRevenueByDateRange(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate) {
        Double revenue = hoaDonService.getTotalRevenueByDateRange(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/status-options")
    public ResponseEntity<Map<String, Object>> getStatusOptions() {
        Map<String, Object> options = Map.of(
                "statuses", Map.of(
                        OrderStatusUtil.CHO_XAC_NHAN, OrderStatusUtil.getStatusName(OrderStatusUtil.CHO_XAC_NHAN),
                        OrderStatusUtil.CHO_GIAO_HANG, OrderStatusUtil.getStatusName(OrderStatusUtil.CHO_GIAO_HANG),
                        OrderStatusUtil.DANG_GIAO, OrderStatusUtil.getStatusName(OrderStatusUtil.DANG_GIAO),
                        OrderStatusUtil.HOAN_THANH, OrderStatusUtil.getStatusName(OrderStatusUtil.HOAN_THANH),
                        OrderStatusUtil.DA_THANH_TOAN_CHO_XAC_NHAN, OrderStatusUtil.getStatusName(OrderStatusUtil.DA_THANH_TOAN_CHO_XAC_NHAN),
                        OrderStatusUtil.DA_HUY, OrderStatusUtil.getStatusName(OrderStatusUtil.DA_HUY)
                ),
                "orderTypes", Map.of(
                        OrderStatusUtil.NORMAL, "Bán tại quầy",
                        OrderStatusUtil.DELIVERY, "Bán online"
                )
        );
        return ResponseEntity.ok(options);
    }

    @PostMapping("/update-sample-data")
    public ResponseEntity<String> updateSampleData() {
        try {
            // Cập nhật dữ liệu mẫu với các trạng thái đa dạng
            hoaDonService.updateSampleDataWithDiverseStatuses();
            return ResponseEntity.ok("Dữ liệu mẫu đã được cập nhật thành công");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi khi cập nhật dữ liệu: " + e.getMessage());
        }
    }

    @GetMapping("/test-tracking/{maHoaDon}")
    public ResponseEntity<?> testTracking(@PathVariable String maHoaDon) {
        try {
            // Test endpoint đơn giản để debug
            var hoaDon = hoaDonService.getByMaHoaDon(maHoaDon);
            if (hoaDon != null) {
                return ResponseEntity.ok(Map.of(
                        "found", true,
                        "maHoaDon", hoaDon.getMaHoaDon(),
                        "tenKhachHang", hoaDon.getTenKhachHang(),
                        "trangThai", hoaDon.getTrangThai(),
                        "loaiHoaDon", hoaDon.getLoaiHoaDon()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "found", false,
                        "maHoaDon", maHoaDon,
                        "message", "Không tìm thấy hóa đơn"
                ));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi test tracking",
                    "message", e.getMessage(),
                    "maHoaDon", maHoaDon
            ));
        }
    }

    @GetMapping("/test-status")
    public ResponseEntity<?> testStatus() {
        try {
            // Test endpoint để kiểm tra trạng thái của tất cả hóa đơn
            var allHoaDons = hoaDonService.getAll();

            return ResponseEntity.ok(Map.of(
                    "totalHoaDons", allHoaDons.size(),
                    "message", "Test successful"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi test status",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/create-test-data")
    public ResponseEntity<?> createTestData() {
        try {
            // Tạo một số hóa đơn test với trạng thái khác nhau
            hoaDonService.updateSampleDataWithDiverseStatuses();

            return ResponseEntity.ok(Map.of(
                    "message", "Test data created successfully",
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi tạo test data",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/create-status-0")
    public ResponseEntity<?> createStatus0() {
        try {
            // Tạo một hóa đơn với trạng thái 0 (Chờ xác nhận)
            HoaDon hoaDon = new HoaDon();
            hoaDon.setMaHoaDon("TEST_STATUS_0_" + System.currentTimeMillis());
            hoaDon.setTenKhachHang("Test Customer Status 0");
            hoaDon.setSoDienThoai("0900000000");
            hoaDon.setDiaChi("Test Address Status 0");
            hoaDon.setTongTien(1000000.0);
            hoaDon.setTongTienSauGiam(1000000.0);
            hoaDon.setLoaiHoaDon("BAN_THUONG");
            hoaDon.setTrangThai(0); // Chờ xác nhận
            hoaDon.setGhiChu("Test data with status 0");
            hoaDon.setNguoiTao("admin");
            hoaDon.setNgayTao(java.time.LocalDateTime.now());
            hoaDon.setNgayCapNhat(java.time.LocalDateTime.now());

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

            return ResponseEntity.ok(Map.of(
                    "message", "Hóa đơn với trạng thái 0 đã được tạo",
                    "maHoaDon", savedHoaDon.getMaHoaDon(),
                    "trangThai", savedHoaDon.getTrangThai(),
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Lỗi khi tạo hóa đơn status 0",
                    "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/delete-test-data")
    public ResponseEntity<?> deleteTestData() {
        try {
            // Xóa tất cả hóa đơn test với maHoaDon bắt đầu bằng "TEST_STATUS_0_"
            List<HoaDon> allHoaDons = hoaDonRepository.findAll();
            System.out.println("Total hóa đơn: " + allHoaDons.size());

            List<HoaDon> testHoaDons = allHoaDons.stream()
                    .filter(hd -> hd.getMaHoaDon().startsWith("TEST_STATUS_0_"))
                    .toList();

            System.out.println("Found " + testHoaDons.size() + " test hóa đơn to delete");

            int deletedCount = 0;
            for (HoaDon hoaDon : testHoaDons) {
                try {
                    System.out.println("Attempting to delete: " + hoaDon.getMaHoaDon() + " (ID: " + hoaDon.getId() + ")");
                    hoaDonRepository.deleteById(hoaDon.getId());
                    deletedCount++;
                    System.out.println("Successfully deleted: " + hoaDon.getMaHoaDon());
                } catch (Exception e) {
                    System.out.println("Error deleting " + hoaDon.getMaHoaDon() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Deleted " + deletedCount + " test invoices",
                    "deletedCount", deletedCount,
                    "status", "success"
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Error deleting test data",
                    "message", e.getMessage()
            ));
        }
    }
}

