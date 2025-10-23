package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.ChipDTO;
import com.example.datn_sd28_2025.service.ChipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chip")
@CrossOrigin(origins = "*")
public class ChipController {

    @Autowired
    private ChipService chipService;

    @GetMapping
    public ResponseEntity<List<ChipDTO>> getAll() {
        List<ChipDTO> chips = chipService.getAll();
        return ResponseEntity.ok(chips);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ChipDTO>> getActive() {
        List<ChipDTO> chips = chipService.getActive();
        return ResponseEntity.ok(chips);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChipDTO> getById(@PathVariable Integer id) {
        Optional<ChipDTO> chip = chipService.getById(id);
        return chip.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<ChipDTO> create(@RequestBody ChipDTO chipDTO) {
        ChipDTO created = chipService.save(chipDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChipDTO> update(@PathVariable Integer id, @RequestBody ChipDTO chipDTO) {
        try {
            ChipDTO updated = chipService.update(id, chipDTO);
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
            chipService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
=======
            if (trangThai == null || (trangThai != 0 && trangThai != 1)) {
                return ResponseEntity.badRequest().body("Trạng thái phải là 0 hoặc 1");
            }
            chipService.updateStatus(id, trangThai);
            return ResponseEntity.ok().body("Cập nhật trạng thái thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
>>>>>>> origin/Huan
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            chipService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}