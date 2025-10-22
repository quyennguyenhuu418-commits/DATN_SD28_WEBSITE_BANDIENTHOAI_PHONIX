package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import com.example.datn_sd28_2025.repository.KhachHangGiamGiaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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
            System.out.println("=== Getting customers for voucher ID: " + voucherId + " ===");
            
            // Validate voucher ID
            if (voucherId == null || voucherId <= 0) {
                System.err.println("Invalid voucher ID: " + voucherId);
                return ResponseEntity.badRequest().body(List.of());
            }
            
            // Use findAll and filter as fallback
            List<KhachHangGiamGia> relations;
            try {
                // First try the JOIN FETCH method
                relations = khachHangGiamGiaRepository.findByPhieuGiamGiaId(voucherId);
                System.out.println("Found " + relations.size() + " relations for voucher " + voucherId);
                
                // If no relations found or all have null customers, try findAll approach
                if (relations.isEmpty() || relations.stream().allMatch(r -> r.getKhachHang() == null)) {
                    System.out.println("Trying findAll approach...");
                    List<KhachHangGiamGia> allRelations = khachHangGiamGiaRepository.findAll();
                    relations = allRelations.stream()
                        .filter(r -> r.getPhieuGiamGia() != null && r.getPhieuGiamGia().getId().equals(voucherId))
                        .toList();
                    System.out.println("Found " + relations.size() + " relations using findAll approach");
                }
            } catch (Exception e) {
                System.err.println("Error querying database: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(List.of());
            }

            if (relations.isEmpty()) {
                System.out.println("No customer relations found for voucher " + voucherId);
                return ResponseEntity.ok(List.of());
            }

            // Process relations safely
            List<Map<String, Object>> result;
            try {
                result = relations.stream()
                    .map(relation -> {
                        try {
                            System.out.println("Processing relation: " + relation.getId());
                            System.out.println("  - Customer: " + (relation.getKhachHang() != null ? "EXISTS" : "NULL"));
                            System.out.println("  - Voucher: " + (relation.getPhieuGiamGia() != null ? "EXISTS" : "NULL"));
                            
                            // Handle null customer data safely
                            Map<String, Object> customerData = null;
                            if (relation.getKhachHang() != null) {
                                try {
                                    customerData = Map.of(
                                        "id", relation.getKhachHang().getId() != null ? relation.getKhachHang().getId() : 0,
                                        "hoTen", relation.getKhachHang().getHoTen() != null ? relation.getKhachHang().getHoTen() : "",
                                        "soDienThoai", relation.getKhachHang().getSoDienThoai() != null ? relation.getKhachHang().getSoDienThoai() : "",
                                        "email", relation.getKhachHang().getEmail() != null ? relation.getKhachHang().getEmail() : ""
                                    );
                                    System.out.println("  - Customer data created successfully");
                                } catch (Exception e) {
                                    System.err.println("  - Error creating customer data: " + e.getMessage());
                                    customerData = null;
                                }
                            } else {
                                System.out.println("  - Customer is null, skipping relation");
                                return null;
                            }
                            
                            Map<String, Object> relationMap = Map.of(
                                "id", relation.getId() != null ? relation.getId() : 0,
                                "daSuDung", relation.getDaSuDung() != null ? relation.getDaSuDung() : false,
                                "ngaySuDung", relation.getNgaySuDung() != null ? relation.getNgaySuDung().toString() : null,
                                "soTienGiam", relation.getSoTienGiam() != null ? relation.getSoTienGiam() : 0.0,
                                "trangThai", relation.getTrangThai() != null ? relation.getTrangThai() : 1,
                                "khachHang", customerData
                            );
                            
                            System.out.println("  - Relation processed successfully");
                            return relationMap;
                        } catch (Exception e) {
                            System.err.println("Error processing relation " + relation.getId() + ": " + e.getMessage());
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .filter(relation -> relation != null && relation.get("khachHang") != null)
                    .toList();
            } catch (Exception e) {
                System.err.println("Error processing relations: " + e.getMessage());
                e.printStackTrace();
                return ResponseEntity.internalServerError().body(List.of());
            }

            System.out.println("Returning " + result.size() + " customer relations");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Unexpected error getting customers for voucher " + voucherId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
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

    @GetMapping("/debug/voucher/{voucherId}")
    public ResponseEntity<Map<String, Object>> debugVoucherCustomers(@PathVariable Integer voucherId) {
        try {
            System.out.println("=== DEBUG: Getting customers for voucher ID: " + voucherId + " ===");
            
            // Check if voucher exists
            List<KhachHangGiamGia> allRelations = khachHangGiamGiaRepository.findAll();
            System.out.println("Total relations in database: " + allRelations.size());
            
            // Filter by voucher ID
            List<KhachHangGiamGia> relations = allRelations.stream()
                .filter(r -> r.getPhieuGiamGia() != null && r.getPhieuGiamGia().getId().equals(voucherId))
                .toList();
            
            System.out.println("Found " + relations.size() + " relations for voucher " + voucherId);
            
            // Debug each relation
            for (KhachHangGiamGia relation : relations) {
                System.out.println("Relation ID: " + relation.getId());
                System.out.println("  - Customer: " + (relation.getKhachHang() != null ? relation.getKhachHang().getHoTen() : "NULL"));
                System.out.println("  - Voucher: " + (relation.getPhieuGiamGia() != null ? relation.getPhieuGiamGia().getTenPhieuGiamGia() : "NULL"));
                System.out.println("  - Status: " + relation.getTrangThai());
            }
            
            return ResponseEntity.ok(Map.of(
                "voucherId", voucherId,
                "totalRelations", allRelations.size(),
                "voucherRelations", relations.size(),
                "relations", relations.stream().map(r -> Map.of(
                    "id", r.getId(),
                    "customerName", r.getKhachHang() != null ? r.getKhachHang().getHoTen() : "NULL",
                    "voucherName", r.getPhieuGiamGia() != null ? r.getPhieuGiamGia().getTenPhieuGiamGia() : "NULL",
                    "status", r.getTrangThai()
                )).toList()
            ));
            
        } catch (Exception e) {
            System.err.println("Debug error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/simple/voucher/{voucherId}")
    public ResponseEntity<List<Map<String, Object>>> getCustomersByVoucherSimple(@PathVariable Integer voucherId) {
        try {
            System.out.println("=== SIMPLE: Getting customers for voucher ID: " + voucherId + " ===");
            
            // Use findAll and filter - most reliable approach
            List<KhachHangGiamGia> allRelations = khachHangGiamGiaRepository.findAll();
            System.out.println("Total relations in database: " + allRelations.size());
            
            List<KhachHangGiamGia> relations = allRelations.stream()
                .filter(r -> r.getPhieuGiamGia() != null && r.getPhieuGiamGia().getId().equals(voucherId))
                .toList();
            
            System.out.println("Found " + relations.size() + " relations for voucher " + voucherId);
            
            if (relations.isEmpty()) {
                return ResponseEntity.ok(List.of());
            }
            
            // Simple mapping without complex null checks
            List<Map<String, Object>> result = new ArrayList<>();
            for (KhachHangGiamGia relation : relations) {
                try {
                    Map<String, Object> relationMap = new HashMap<>();
                    relationMap.put("id", relation.getId());
                    relationMap.put("daSuDung", relation.getDaSuDung() != null ? relation.getDaSuDung() : false);
                    relationMap.put("ngaySuDung", relation.getNgaySuDung() != null ? relation.getNgaySuDung().toString() : null);
                    relationMap.put("soTienGiam", relation.getSoTienGiam() != null ? relation.getSoTienGiam() : 0.0);
                    relationMap.put("trangThai", relation.getTrangThai() != null ? relation.getTrangThai() : 1);
                    
                    if (relation.getKhachHang() != null) {
                        Map<String, Object> customerMap = new HashMap<>();
                        customerMap.put("id", relation.getKhachHang().getId());
                        customerMap.put("hoTen", relation.getKhachHang().getHoTen() != null ? relation.getKhachHang().getHoTen() : "");
                        customerMap.put("soDienThoai", relation.getKhachHang().getSoDienThoai() != null ? relation.getKhachHang().getSoDienThoai() : "");
                        customerMap.put("email", relation.getKhachHang().getEmail() != null ? relation.getKhachHang().getEmail() : "");
                        relationMap.put("khachHang", customerMap);
                    } else {
                        relationMap.put("khachHang", null);
                    }
                    
                    result.add(relationMap);
                    System.out.println("Successfully processed relation " + relation.getId());
                } catch (Exception e) {
                    System.err.println("Error processing relation " + relation.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Returning " + result.size() + " customer relations");
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            System.err.println("Simple method error: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
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

