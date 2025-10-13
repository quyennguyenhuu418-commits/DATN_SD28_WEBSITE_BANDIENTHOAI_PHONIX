package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hoa-don")
@CrossOrigin(origins = "*")
public class HoaDonController {

    @Autowired
    private HoaDonService hoaDonService;

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

    @PutMapping("/{id}")
    public ResponseEntity<HoaDonDTO> update(@PathVariable Integer id, @RequestBody HoaDonDTO hoaDonDTO) {
        try {
            HoaDonDTO updated = hoaDonService.updateHoaDon(id, hoaDonDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
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
                OrderStatusUtil.NORMAL, "Bán trực tiếp",
                OrderStatusUtil.DELIVERY, "Giao hàng"
            )
        );
        return ResponseEntity.ok(options);
    }
}

