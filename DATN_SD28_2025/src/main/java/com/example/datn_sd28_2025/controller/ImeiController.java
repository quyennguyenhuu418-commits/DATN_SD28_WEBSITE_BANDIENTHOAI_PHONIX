package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.service.ImeiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/imei")
@CrossOrigin(origins = "*")
public class ImeiController {

    private final ImeiService imeiService;
    private final ImeiDaBanRepository imeiDaBanRepository;

    public ImeiController(ImeiService imeiService, ImeiDaBanRepository imeiDaBanRepository) {
        this.imeiService = imeiService;
        this.imeiDaBanRepository = imeiDaBanRepository;
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
    
    @PostMapping("/mark-sold")
    public ResponseEntity<Map<String, Object>> markImeiAsSold(@RequestBody Map<String, Object> request) {
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
            Imei imeiEntity = imeiService.findByImeiString(imei);
            if (imeiEntity == null) {
                System.out.println("❌ IMEI not found: " + imei);
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Không tìm thấy IMEI: " + imei));
            }
            
            System.out.println("✅ Found IMEI entity: " + imeiEntity.getId());
            
            // Cập nhật trạng thái trong bảng imei
            System.out.println("🔍 Updating IMEI status to: " + trangThai);
            imeiEntity.setTrangThai(trangThai);
            ImeiDTO imeiDTO = imeiService.convertToDTO(imeiEntity);
            imeiService.update(imeiEntity.getId(), imeiDTO);
            
            System.out.println("✅ Updated IMEI table");
            
            // Cập nhật trạng thái trong bảng imei_da_ban
            System.out.println("🔍 Finding ImeiDaBan by IMEI: " + imei);
            ImeiDaBan imeiDaBan = imeiDaBanRepository.findByImei(imei);
            if (imeiDaBan != null) {
                System.out.println("✅ Found ImeiDaBan: " + imeiDaBan.getId());
                imeiDaBan.setTrangThai(trangThai);
                imeiDaBanRepository.save(imeiDaBan);
                System.out.println("✅ Updated ImeiDaBan table");
            } else {
                System.out.println("⚠️ No ImeiDaBan found for IMEI: " + imei);
            }
            
            System.out.println("✅ Successfully processed IMEI cancellation");
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "IMEI đã được đánh dấu là đã bán",
                "imei", imei,
                "trangThai", trangThai
            ));
            
        } catch (Exception e) {
            System.err.println("❌ Error in markImeiAsSold: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Lỗi khi đánh dấu IMEI: " + e.getMessage()));
        }
    }
}


