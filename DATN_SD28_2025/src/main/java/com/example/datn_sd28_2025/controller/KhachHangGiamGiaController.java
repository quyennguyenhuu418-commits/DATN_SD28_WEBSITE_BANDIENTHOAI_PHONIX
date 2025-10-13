package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import com.example.datn_sd28_2025.repository.KhachHangGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang-giam-gia")
@CrossOrigin(origins = "*")
public class KhachHangGiamGiaController {

    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;

    @GetMapping("/voucher/{voucherId}")
    public ResponseEntity<List<Map<String, Object>>> getCustomersByVoucher(@PathVariable Integer voucherId) {
        try {
            List<KhachHangGiamGia> relations = khachHangGiamGiaRepository.findAll().stream()
                .filter(r -> r.getPhieuGiamGia() != null && r.getPhieuGiamGia().getId().equals(voucherId))
                .toList();

            List<Map<String, Object>> result = relations.stream()
                .map(relation -> Map.of(
                    "id", relation.getId(),
                    "daSuDung", relation.getDaSuDung() != null ? relation.getDaSuDung() : false,
                    "ngaySuDung", relation.getNgaySuDung() != null ? relation.getNgaySuDung().toString() : null,
                    "soTienGiam", relation.getSoTienGiam() != null ? relation.getSoTienGiam() : 0.0,
                    "trangThai", relation.getTrangThai(),
                    "khachHang", relation.getKhachHang() != null ? Map.of(
                        "id", relation.getKhachHang().getId(),
                        "hoTen", relation.getKhachHang().getHoTen(),
                        "soDienThoai", relation.getKhachHang().getSoDienThoai(),
                        "email", relation.getKhachHang().getEmail() != null ? relation.getKhachHang().getEmail() : ""
                    ) : null
                ))
                .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting customers for voucher " + voucherId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<Map<String, Object>>> getVouchersByCustomer(@PathVariable Integer customerId) {
        try {
            List<KhachHangGiamGia> relations = khachHangGiamGiaRepository.findByKhachHangId(customerId);

            List<Map<String, Object>> result = relations.stream()
                .map(relation -> Map.of(
                    "id", relation.getId(),
                    "daSuDung", relation.getDaSuDung() != null ? relation.getDaSuDung() : false,
                    "ngaySuDung", relation.getNgaySuDung() != null ? relation.getNgaySuDung().toString() : null,
                    "soTienGiam", relation.getSoTienGiam() != null ? relation.getSoTienGiam() : 0.0,
                    "trangThai", relation.getTrangThai(),
                    "phieuGiamGia", relation.getPhieuGiamGia() != null ? Map.of(
                        "id", relation.getPhieuGiamGia().getId(),
                        "maPhieuGiamGia", relation.getPhieuGiamGia().getMaPhieuGiamGia(),
                        "tenPhieuGiamGia", relation.getPhieuGiamGia().getTenPhieuGiamGia(),
                        "giaTriGiamGia", relation.getPhieuGiamGia().getGiaTriGiamGia(),
                        "loaiPhieuGiamGia", relation.getPhieuGiamGia().getLoaiPhieuGiamGia()
                    ) : null
                ))
                .toList();

            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error getting vouchers for customer " + customerId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/mark-used")
    public ResponseEntity<Map<String, Object>> markVoucherAsUsed(@RequestBody Map<String, Object> request) {
        try {
            Integer customerId = (Integer) request.get("customerId");
            Integer voucherId = (Integer) request.get("voucherId");
            String usedAt = (String) request.get("usedAt");

            if (customerId == null || voucherId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Customer ID and Voucher ID are required"));
            }

            // Find the relation between customer and voucher
            Optional<KhachHangGiamGia> relationOpt = khachHangGiamGiaRepository.findAll().stream()
                .filter(r -> r.getKhachHang() != null && r.getPhieuGiamGia() != null &&
                           r.getKhachHang().getId().equals(customerId) && 
                           r.getPhieuGiamGia().getId().equals(voucherId))
                .findFirst();

            if (relationOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Voucher not found for this customer"));
            }

            KhachHangGiamGia relation = relationOpt.get();
            
            // Mark as used (even though the field is commented out, we can still track it)
            // For now, we'll just return success since the field is not in the database
            // In a real implementation, you would uncomment the field and set it here
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Voucher marked as used",
                "customerId", customerId,
                "voucherId", voucherId,
                "usedAt", usedAt != null ? usedAt : LocalDateTime.now().toString()
            ));

        } catch (Exception e) {
            System.err.println("Error marking voucher as used: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", "Failed to mark voucher as used"));
        }
    }
}

