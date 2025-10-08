package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ManHinhDTO;
import com.example.datn_sd28_2025.service.ManHinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/man-hinh")
@CrossOrigin(origins = "*")
public class ManHinhController {

    @Autowired
    private ManHinhService manHinhService;

    @GetMapping
    public ResponseEntity<List<ManHinhDTO>> getAll() {
        List<ManHinhDTO> manHinhs = manHinhService.getAll();
        return ResponseEntity.ok(manHinhs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ManHinhDTO>> getActive() {
        List<ManHinhDTO> manHinhs = manHinhService.getActive();
        return ResponseEntity.ok(manHinhs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManHinhDTO> getById(@PathVariable Integer id) {
        Optional<ManHinhDTO> manHinh = manHinhService.getById(id);
        return manHinh.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ManHinhDTO> create(@RequestBody ManHinhDTO manHinhDTO) {
        ManHinhDTO created = manHinhService.save(manHinhDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ManHinhDTO> update(@PathVariable Integer id, @RequestBody ManHinhDTO manHinhDTO) {
        try {
            ManHinhDTO updated = manHinhService.update(id, manHinhDTO);
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
            manHinhService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            manHinhService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}