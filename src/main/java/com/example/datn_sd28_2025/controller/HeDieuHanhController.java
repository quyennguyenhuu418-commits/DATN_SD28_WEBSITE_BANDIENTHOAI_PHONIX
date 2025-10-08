package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.HeDieuHanhDTO;
import com.example.datn_sd28_2025.service.HeDieuHanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/he-dieu-hanh")
@CrossOrigin(origins = "*")
public class HeDieuHanhController {

    @Autowired
    private HeDieuHanhService heDieuHanhService;

    @GetMapping
    public ResponseEntity<List<HeDieuHanhDTO>> getAll() {
        List<HeDieuHanhDTO> heDieuHanhs = heDieuHanhService.getAll();
        return ResponseEntity.ok(heDieuHanhs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<HeDieuHanhDTO>> getActive() {
        List<HeDieuHanhDTO> heDieuHanhs = heDieuHanhService.getActive();
        return ResponseEntity.ok(heDieuHanhs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HeDieuHanhDTO> getById(@PathVariable Integer id) {
        Optional<HeDieuHanhDTO> heDieuHanh = heDieuHanhService.getById(id);
        return heDieuHanh.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HeDieuHanhDTO> create(@RequestBody HeDieuHanhDTO heDieuHanhDTO) {
        HeDieuHanhDTO created = heDieuHanhService.save(heDieuHanhDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HeDieuHanhDTO> update(@PathVariable Integer id, @RequestBody HeDieuHanhDTO heDieuHanhDTO) {
        try {
            HeDieuHanhDTO updated = heDieuHanhService.update(id, heDieuHanhDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer trangThai = request.get("trangThai");
            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                return ResponseEntity.badRequest().body("Trạng thái phải là 0 hoặc 1");
            }
            heDieuHanhService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            heDieuHanhService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}