package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.service.ImeiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imei")
@CrossOrigin(origins = "*")
public class ImeiController {

    private final ImeiService imeiService;
    private final ImeiDaBanRepository imeiDaBanRepository;
    private final ImeiRepository imeiRepository;

    public ImeiController(ImeiService imeiService, ImeiDaBanRepository imeiDaBanRepository, ImeiRepository imeiRepository) {
        this.imeiService = imeiService;
        this.imeiDaBanRepository = imeiDaBanRepository;
        this.imeiRepository = imeiRepository;
    }

    @PostMapping
    public ResponseEntity<ImeiDTO> create(@RequestBody ImeiDTO imeiDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(imeiService.save(imeiDTO));
    }

    @PostMapping("/bulk-upload")
    public ResponseEntity<List<Imei>> bulkUpload(@RequestParam Integer chiTietId, @RequestBody String raw) {
        List<String> lines = Arrays.stream(raw.split("\r?\n")).toList();
        return ResponseEntity.status(HttpStatus.CREATED).body(imeiService.bulkCreate(chiTietId, lines));
    }

    @GetMapping("/by-variant/{chiTietId}")
    public ResponseEntity<List<Imei>> getByVariant(@PathVariable Integer chiTietId, @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(imeiService.findByChiTiet(chiTietId, status));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Imei> updateStatus(@PathVariable Integer id, @RequestParam Integer value) {
        return ResponseEntity.ok(imeiService.updateStatus(id, value));
    }

    // DTO endpoints
    @GetMapping
    public ResponseEntity<List<ImeiDTO>> getAll() {
        List<ImeiDTO> imeis = imeiService.getAll();
        return ResponseEntity.ok(imeis);
    }

    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateImei(@RequestParam String imei) {
        try {
            // Check if IMEI exists in imei table (available for sale)
            Imei imeiEntity = imeiService.findByImeiString(imei.trim());
            boolean exists = imeiEntity != null;
            
            String status = "not_found";
            String assignedTo = null;
            
            if (exists) {
                // Check if IMEI is available (trangThai = 1)
                if (imeiEntity.getTrangThai() == 1) {
                    status = "available";
                } else {
                    status = "sold";
                }
                
                // Check if IMEI is also assigned to any order line in imei_da_ban
                List<ImeiDaBan> assignedImeis = imeiDaBanRepository.findByImei(imei.trim());
                ImeiDaBan assignedImei = assignedImeis.stream()
                    .filter(imeiDaBan -> imeiDaBan.getHoaDonChiTiet() != null)
                    .findFirst().orElse(null);
                
                if (assignedImei != null) {
                    status = "assigned";
                    assignedTo = "Order line ID: " + assignedImei.getHoaDonChiTiet().getId();
                }
            }
            
            return ResponseEntity.ok(Map.of(
                "exists", exists,
                "status", status,
                "assignedTo", assignedTo,
                "imei", imei,
                "message", status.equals("assigned") ? "IMEI đã được gán cho đơn hàng khác" : 
                         status.equals("available") ? "IMEI có thể sử dụng" : 
                         status.equals("sold") ? "IMEI đã bán" : "IMEI không tồn tại"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Lỗi khi validate IMEI",
                "message", e.getMessage()
            ));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImeiDTO> getById(@PathVariable Integer id) {
        ImeiDTO imei = imeiService.getById(id);
        if (imei != null) {
            return ResponseEntity.ok(imei);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ImeiDTO> update(@PathVariable Integer id, @RequestBody ImeiDTO imeiDTO) {
        try {
            ImeiDTO updated = imeiService.update(id, imeiDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            imeiService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/check-duplicates")
    public ResponseEntity<List<String>> checkDuplicates(@RequestBody List<String> imeis) {
        List<String> duplicates = imeiService.findDuplicateImeis(imeis);
        return ResponseEntity.ok(duplicates);
    }
    
    @GetMapping("/chi-tiet/{chiTietSanPhamId}")
    public ResponseEntity<List<ImeiDTO>> getByChiTietSanPham(@PathVariable Integer chiTietSanPhamId) {
        List<ImeiDTO> imeis = imeiService.getByChiTietSanPham(chiTietSanPhamId);
        return ResponseEntity.ok(imeis);
    }
    
    @GetMapping("/available")
    public ResponseEntity<List<Map<String, Object>>> getAvailableImeis() {
        try {
            // Get all IMEIs that are not assigned to any order line
            List<ImeiDaBan> allImeis = imeiDaBanRepository.findAll();
            List<Map<String, Object>> availableImeis = allImeis.stream()
                .filter(imeiDaBan -> imeiDaBan.getHoaDonChiTiet() == null)
                .map(imeiDaBan -> {
                    Map<String, Object> imeiInfo = new HashMap<>();
                    imeiInfo.put("id", imeiDaBan.getId());
                    imeiInfo.put("imei", imeiDaBan.getImei());
                    imeiInfo.put("trangThai", imeiDaBan.getTrangThai());
                    imeiInfo.put("status", "available");
                    return imeiInfo;
                })
                .toList();
            
            return ResponseEntity.ok(availableImeis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(Map.of(
                "error", "Lỗi khi lấy danh sách IMEI khả dụng",
                "message", e.getMessage()
            )));
        }
    }
    
    @GetMapping("/product/{chiTietSanPhamId}")
    public ResponseEntity<List<Map<String, Object>>> getImeisByProduct(@PathVariable Integer chiTietSanPhamId) {
        try {
            // Get IMEIs for a specific product variant with status information
            List<Imei> imeis = imeiRepository.findByChiTietSanPhamId(chiTietSanPhamId);
            List<Map<String, Object>> imeiList = imeis.stream()
                .map(imei -> {
                    Map<String, Object> imeiInfo = new HashMap<>();
                    imeiInfo.put("id", imei.getId());
                    imeiInfo.put("imei", imei.getImei());
                    imeiInfo.put("trangThai", imei.getTrangThai());
                    imeiInfo.put("status", imei.getTrangThai() == 1 ? "available" : "sold");
                    imeiInfo.put("chiTietSanPhamId", chiTietSanPhamId);
                    return imeiInfo;
                })
                .toList();
            
            return ResponseEntity.ok(imeiList);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(List.of(Map.of(
                "error", "Lỗi khi lấy danh sách IMEI cho sản phẩm",
                "message", e.getMessage()
            )));
        }
    }
    
    @PostMapping("/mark-sold")
    public ResponseEntity<Map<String, Object>> markImeiAsSold(@RequestBody Map<String, Object> request) {
        System.out.println("🚀 ImeiController.markImeiAsSold method called!");
        System.out.println("🚀 Request received: " + request);
        System.out.println("🚀 Current time: " + java.time.LocalDateTime.now());
        System.out.println("🚀 Request method: POST");
        System.out.println("🚀 Request URL: /api/imei/mark-sold");
        System.out.println("🚀 Request body type: " + request.getClass().getName());
        try {
            System.out.println("🔍 ImeiController.markImeiAsSold called with request: " + request);
            
            String imei = (String) request.get("imei");
            Integer trangThai = (Integer) request.get("trangThai");
            
            System.out.println("🔍 IMEI: " + imei + ", TrangThai: " + trangThai);
            
            if (imei == null || trangThai == null) {
                System.out.println("❌ Missing imei or trangThai");
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Thiếu thông tin imei hoặc trangThai"));
            }
            
            // Tìm IMEI theo imei string
            System.out.println("🔍 Finding IMEI by string: " + imei);
            try {
                Imei imeiEntity = imeiService.findByImeiString(imei);
                if (imeiEntity == null) {
                    System.out.println("❌ IMEI not found: " + imei);
                    return ResponseEntity.badRequest()
                            .body(Map.of("success", false, "message", "Không tìm thấy IMEI: " + imei));
                }
                
                System.out.println("✅ Found IMEI entity: " + imeiEntity.getId());
                
                // Cập nhật trạng thái trong bảng imei
                System.out.println("🔍 Updating IMEI status to: " + trangThai);
                try {
                    imeiEntity.setTrangThai(trangThai);
                    ImeiDTO imeiDTO = imeiService.convertToDTO(imeiEntity);
                    imeiService.update(imeiEntity.getId(), imeiDTO);
                    System.out.println("✅ Updated IMEI table");
                } catch (Exception e) {
                    System.err.println("❌ Error updating IMEI table: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                
                // Cập nhật trạng thái trong bảng imei_da_ban
                System.out.println("🔍 Finding ImeiDaBan by IMEI: " + imei);
                try {
                    List<ImeiDaBan> imeiDaBanList = imeiDaBanRepository.findByImei(imei);
                    if (imeiDaBanList != null && !imeiDaBanList.isEmpty()) {
                        System.out.println("✅ Found " + imeiDaBanList.size() + " ImeiDaBan records for IMEI: " + imei);
                        for (ImeiDaBan imeiDaBan : imeiDaBanList) {
                            System.out.println("✅ Updating ImeiDaBan ID: " + imeiDaBan.getId());
                            imeiDaBan.setTrangThai(trangThai);
                            imeiDaBanRepository.save(imeiDaBan);
                        }
                        System.out.println("✅ Updated all ImeiDaBan records");
                    } else {
                        System.out.println("⚠️ No ImeiDaBan found for IMEI: " + imei);
                    }
                } catch (Exception e) {
                    System.err.println("❌ Error updating ImeiDaBan table: " + e.getMessage());
                    e.printStackTrace();
                    throw e;
                }
                
                System.out.println("✅ Successfully processed IMEI cancellation");
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "IMEI đã được đánh dấu là đã bán",
                    "imei", imei,
                    "trangThai", trangThai
                ));
                
            } catch (Exception e) {
                System.err.println("❌ Error in IMEI processing: " + e.getMessage());
                e.printStackTrace();
                throw e;
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error in markImeiAsSold: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi đánh dấu IMEI: " + e.getMessage()));
        }
    }
}


