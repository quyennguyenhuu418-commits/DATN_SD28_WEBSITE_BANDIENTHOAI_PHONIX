package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khuyen-mai")
@CrossOrigin(origins = "*")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @GetMapping
    public ResponseEntity<List<KhuyenMaiDTO>> getAll() {
        List<KhuyenMaiDTO> khuyenMais = khuyenMaiService.getAll();
        return ResponseEntity.ok(khuyenMais);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhuyenMaiDTO> getById(@PathVariable Integer id) {
        Optional<KhuyenMaiDTO> khuyenMai = khuyenMaiService.getById(id);
        return khuyenMai.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/active")
    public ResponseEntity<List<KhuyenMaiDTO>> getActivePromotions() {
        List<KhuyenMaiDTO> activePromotions = khuyenMaiService.getActivePromotions();
        return ResponseEntity.ok(activePromotions);
    }

    @GetMapping("/by-code/{code}")
    public ResponseEntity<KhuyenMaiDTO> getByCode(@PathVariable String code) {
        Optional<KhuyenMaiDTO> khuyenMai = khuyenMaiService.getByCode(code);
        return khuyenMai.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<KhuyenMaiDTO> create(@RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        try {
            KhuyenMaiDTO created = khuyenMaiService.save(khuyenMaiDTO);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhuyenMaiDTO> update(@PathVariable Integer id, @RequestBody KhuyenMaiDTO khuyenMaiDTO) {
        try {
            KhuyenMaiDTO updated = khuyenMaiService.update(id, khuyenMaiDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            khuyenMaiService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/toggle-status")
    public ResponseEntity<KhuyenMaiDTO> toggleStatus(@PathVariable Integer id) {
        try {
            KhuyenMaiDTO updated = khuyenMaiService.toggleStatus(id);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

