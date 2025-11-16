package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.HangDTO;
import com.example.datn_sd28_2025.service.HangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/hang")
@CrossOrigin(origins = "*")
public class HangController {

    @Autowired
    private HangService hangService;

    @GetMapping
    public ResponseEntity<List<HangDTO>> getAll() {
        List<HangDTO> hangs = hangService.getAll();
        return ResponseEntity.ok(hangs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<HangDTO>> getActive() {
        List<HangDTO> hangs = hangService.getActive();
        return ResponseEntity.ok(hangs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HangDTO> getById(@PathVariable Integer id) {
        Optional<HangDTO> hang = hangService.getById(id);
        return hang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<HangDTO> create(@RequestBody HangDTO hangDTO) {
        HangDTO created = hangService.save(hangDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<HangDTO> update(@PathVariable Integer id, @RequestBody HangDTO hangDTO) {
        try {
            HangDTO updated = hangService.update(id, hangDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Integer id, @RequestBody Map<String, Integer> request) {
        try {
            Integer trangThai = request.get("trangThai");
            if (trangThai == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "trangThai is required"));
            }
            hangService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            hangService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}