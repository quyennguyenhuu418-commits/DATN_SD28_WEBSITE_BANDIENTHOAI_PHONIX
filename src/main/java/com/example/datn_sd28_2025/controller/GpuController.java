package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.GpuDTO;
import com.example.datn_sd28_2025.service.GpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
<<<<<<< HEAD
import java.util.Optional;
=======

>>>>>>> origin/Huan

@RestController
@RequestMapping("/api/gpu")
@CrossOrigin(origins = "*")
public class GpuController {

    @Autowired
    private GpuService gpuService;

    @GetMapping
    public ResponseEntity<List<GpuDTO>> getAll() {
        return ResponseEntity.ok(gpuService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<GpuDTO>> getActive() {
        return ResponseEntity.ok(gpuService.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GpuDTO> getById(@PathVariable Integer id) {
        return gpuService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<GpuDTO> create(@RequestBody GpuDTO gpuDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gpuService.save(gpuDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GpuDTO> update(@PathVariable Integer id, @RequestBody GpuDTO gpuDTO) {
        try {
            return ResponseEntity.ok(gpuService.update(id, gpuDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer trangThai = request.get("trangThai");
<<<<<<< HEAD
            if (trangThai == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "trangThai is required"));
            }
            gpuService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
=======
            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                return ResponseEntity.badRequest().body("Trạng thái phải là 0 hoặc 1");
            }
            gpuService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
>>>>>>> origin/Huan
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            gpuService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
