package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CameraTruocDTO;
import com.example.datn_sd28_2025.service.CameraTruocService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/camera-truoc")
@CrossOrigin(origins = "*")
public class CameraTruocController {

    @Autowired
    private CameraTruocService cameraTruocService;

    @GetMapping
    public ResponseEntity<List<CameraTruocDTO>> getAll() {
        List<CameraTruocDTO> cameras = cameraTruocService.getAll();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/active")
    public ResponseEntity<List<CameraTruocDTO>> getActive() {
        List<CameraTruocDTO> cameras = cameraTruocService.getActive();
        return ResponseEntity.ok(cameras);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CameraTruocDTO> getById(@PathVariable Integer id) {
        Optional<CameraTruocDTO> camera = cameraTruocService.getById(id);
        return camera.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CameraTruocDTO> create(@RequestBody CameraTruocDTO cameraTruocDTO) {
        CameraTruocDTO created = cameraTruocService.save(cameraTruocDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CameraTruocDTO> update(@PathVariable Integer id, @RequestBody CameraTruocDTO cameraTruocDTO) {
        try {
            CameraTruocDTO updated = cameraTruocService.update(id, cameraTruocDTO);
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
            cameraTruocService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            cameraTruocService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
