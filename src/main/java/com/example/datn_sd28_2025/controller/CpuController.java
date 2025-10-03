package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CpuDTO;
import com.example.datn_sd28_2025.service.CpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/cpu")
@CrossOrigin(origins = "*")
public class CpuController {

    @Autowired
    private CpuService cpuService;

    @GetMapping
    public ResponseEntity<List<CpuDTO>> getAll() {
        return ResponseEntity.ok(cpuService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CpuDTO> getById(@PathVariable Integer id) {
        return cpuService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CpuDTO> create(@RequestBody CpuDTO cpuDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cpuService.save(cpuDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CpuDTO> update(@PathVariable Integer id, @RequestBody CpuDTO cpuDTO) {
        try {
            return ResponseEntity.ok(cpuService.update(id, cpuDTO));
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
            cpuService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            cpuService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
