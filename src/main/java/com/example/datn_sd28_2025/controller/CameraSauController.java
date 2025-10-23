package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CameraSauDTO;
import com.example.datn_sd28_2025.service.CameraSauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/camera-sau")
@CrossOrigin(origins = "*")
public class CameraSauController {

    @Autowired
    private CameraSauService cameraSauService;

    @GetMapping
    public ResponseEntity<List<CameraSauDTO>> getAll() {
        List<CameraSauDTO> cameras = cameraSauService.getAll();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CameraSauDTO>> getActive() {
        List<CameraSauDTO> cameras = cameraSauService.getActive();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraSauDTO> getById(@PathVariable Integer id) {
        Optional<CameraSauDTO> camera = cameraSauService.getById(id);
        return camera.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CameraSauDTO> create(@RequestBody CameraSauDTO cameraSauDTO) {
        CameraSauDTO created = cameraSauService.save(cameraSauDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraSauDTO> update(@PathVariable Integer id, @RequestBody CameraSauDTO cameraSauDTO) {
        try {
            CameraSauDTO updated = cameraSauService.update(id, cameraSauDTO);
            return ResponseEntity.ok(updated);
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
            cameraSauService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
=======
            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                return ResponseEntity.badRequest().body("Trạng thái phải là 0 hoặc 1");
            }
            cameraSauService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
>>>>>>> origin/Huan
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            cameraSauService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
