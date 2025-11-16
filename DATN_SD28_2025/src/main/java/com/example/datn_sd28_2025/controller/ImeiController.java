package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.service.ImeiService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/imei")
@CrossOrigin(origins = "*")
public class ImeiController {

    private final ImeiService imeiService;

    public ImeiController(ImeiService imeiService) {
        this.imeiService = imeiService;
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
}


