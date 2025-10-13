package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.PhieuGiamGiaDTO;
import com.example.datn_sd28_2025.dto.VoucherUsageRequest;
import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import com.example.datn_sd28_2025.repository.KhachHangGiamGiaRepository;
import com.example.datn_sd28_2025.service.PhieuGiamGiaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/phieu-giam-gia")
@CrossOrigin(origins = "*")
public class PhieuGiamGiaController {

    @Autowired
    private PhieuGiamGiaService phieuGiamGiaService;
    
    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;

    @GetMapping
    public ResponseEntity<List<PhieuGiamGiaDTO>> getAll() {
        List<PhieuGiamGiaDTO> phieuGiamGias = phieuGiamGiaService.getAll();
        return ResponseEntity.ok(phieuGiamGias);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaDTO> getById(@PathVariable Integer id) {
        Optional<PhieuGiamGiaDTO> phieuGiamGia = phieuGiamGiaService.getById(id);
        return phieuGiamGia.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<PhieuGiamGiaDTO>> getActiveVouchers() {
        List<PhieuGiamGiaDTO> activeVouchers = phieuGiamGiaService.getActiveVouchers();
        return ResponseEntity.ok(activeVouchers);
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<PhieuGiamGiaDTO> getByCode(@PathVariable String code) {
        Optional<PhieuGiamGiaDTO> phieuGiamGia = phieuGiamGiaService.getByCode(code);
        return phieuGiamGia.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PhieuGiamGiaDTO> create(@RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO) {
        try {
            PhieuGiamGiaDTO created = phieuGiamGiaService.save(phieuGiamGiaDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<PhieuGiamGiaDTO> update(@PathVariable Integer id, @RequestBody PhieuGiamGiaDTO phieuGiamGiaDTO) {
        System.out.println("=== PUT REQUEST RECEIVED ===");
        System.out.println("ID: " + id);
        System.out.println("Request body received: " + (phieuGiamGiaDTO != null));
        
        try {
            if (phieuGiamGiaDTO == null) {
                System.err.println("Request body is null!");
                return ResponseEntity.badRequest().build();
            }
            
            System.out.println("Voucher name: " + phieuGiamGiaDTO.getTenPhieuGiamGia());
            System.out.println("Is private: " + phieuGiamGiaDTO.getRiengTu());
            
            PhieuGiamGiaDTO updated = phieuGiamGiaService.update(id, phieuGiamGiaDTO);
            System.out.println("Update successful");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            System.err.println("RuntimeException in update: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Exception in update: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            phieuGiamGiaService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Integer id) {
        try {
            PhieuGiamGiaDTO updated = phieuGiamGiaService.toggleStatus(id);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<PhieuGiamGiaDTO>> getVouchersByCustomer(@PathVariable Integer customerId) {
        try {
            System.out.println("Getting vouchers for customer: " + customerId);
            List<PhieuGiamGiaDTO> vouchers = phieuGiamGiaService.getVouchersByCustomer(customerId);
            System.out.println("Found " + vouchers.size() + " vouchers");
            return ResponseEntity.ok(vouchers);
        } catch (Exception e) {
            System.err.println("Error in getVouchersByCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new java.util.ArrayList<>());
        }
    }

    // Simple version for testing - just return all active vouchers
    @GetMapping("/customer/{customerId}/simple")
    public ResponseEntity<List<PhieuGiamGiaDTO>> getVouchersByCustomerSimple(@PathVariable Integer customerId) {
        try {
            System.out.println("Getting ALL active vouchers (simple version) for customer: " + customerId);
            List<PhieuGiamGiaDTO> allVouchers = phieuGiamGiaService.getAll();
            System.out.println("Total vouchers in system: " + allVouchers.size());
            
            // Just return active vouchers without complex filtering
            List<PhieuGiamGiaDTO> activeVouchers = allVouchers.stream()
                .filter(v -> v.getTrangThai() == 1)
                .toList();
            
            System.out.println("Active vouchers: " + activeVouchers.size());
            return ResponseEntity.ok(activeVouchers);
        } catch (Exception e) {
            System.err.println("Error in getVouchersByCustomerSimple: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new java.util.ArrayList<>());
        }
    }

    @GetMapping("/customer/{customerId}/used")
    public ResponseEntity<List<PhieuGiamGiaDTO>> getUsedVouchersByCustomer(@PathVariable Integer customerId) {
        try {
            System.out.println("Getting used vouchers for customer: " + customerId);
            List<PhieuGiamGiaDTO> usedVouchers = phieuGiamGiaService.getUsedVouchersByCustomer(customerId);
            System.out.println("Found " + usedVouchers.size() + " used vouchers");
            return ResponseEntity.ok(usedVouchers);
        } catch (Exception e) {
            System.err.println("Error in getUsedVouchersByCustomer: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(new java.util.ArrayList<>());
        }
    }

    @PostMapping("/validate-usage")
    public ResponseEntity<?> validateVoucherUsage(@RequestBody VoucherUsageRequest request) {
        try {
            boolean isValid = phieuGiamGiaService.canCustomerUseVoucher(request.getCustomerId(), request.getVoucherCode());
            if (isValid) {
                return ResponseEntity.ok().body("Voucher có thể sử dụng");
            } else {
                return ResponseEntity.badRequest().body("Khách hàng không có quyền sử dụng voucher này hoặc đã sử dụng rồi");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/mark-used")
    public ResponseEntity<?> markVoucherAsUsed(@RequestBody VoucherUsageRequest request) {
        try {
            boolean success = phieuGiamGiaService.markVoucherAsUsed(
                request.getCustomerId(), 
                request.getVoucherCode(), 
                request.getOrderAmount()
            );
            
            if (success) {
                return ResponseEntity.ok().body("Đã đánh dấu voucher đã sử dụng");
            } else {
                return ResponseEntity.badRequest().body("Không thể đánh dấu voucher đã sử dụng");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Debug endpoint
    @GetMapping("/debug/all")
    public ResponseEntity<?> getAllVouchersDebug() {
        try {
            List<PhieuGiamGiaDTO> allVouchers = phieuGiamGiaService.getAll();
            return ResponseEntity.ok(Map.of(
                "totalVouchers", allVouchers.size(),
                "vouchers", allVouchers
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Simple test endpoint
    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        return ResponseEntity.ok(Map.of(
            "message", "API is working",
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }

    @GetMapping("/debug/customer/{customerId}/relations")
    public ResponseEntity<?> getCustomerRelationsDebug(@PathVariable Integer customerId) {
        try {
            // Get customer voucher relations
            List<KhachHangGiamGia> relations = khachHangGiamGiaRepository.findByKhachHangId(customerId);
            
            Map<String, Object> debugInfo = Map.of(
                "customerId", customerId,
                "totalRelations", relations.size(),
                "relations", relations.stream().map(r -> Map.of(
                    "id", r.getId(),
                    "voucherId", r.getPhieuGiamGia() != null ? r.getPhieuGiamGia().getId() : "null",
                    "voucherCode", r.getPhieuGiamGia() != null ? r.getPhieuGiamGia().getMaPhieuGiamGia() : "null",
                    "used", r.getDaSuDung(),
                    "status", r.getTrangThai()
                )).toList()
            );
            
            return ResponseEntity.ok(debugInfo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
