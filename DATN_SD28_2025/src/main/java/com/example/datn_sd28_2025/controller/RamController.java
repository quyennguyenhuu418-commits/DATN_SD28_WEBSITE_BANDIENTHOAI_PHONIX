package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.RamDTO;
import com.example.datn_sd28_2025.service.RamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ram")
@CrossOrigin(origins = "*")
public class RamController {

    @Autowired
    private RamService ramService;

    @GetMapping
    public ResponseEntity<List<RamDTO>> getAll() {
        List<RamDTO> rams = ramService.getAll();
        return ResponseEntity.ok(rams);
    }

    @GetMapping("/active")
    public ResponseEntity<List<RamDTO>> getActive() {
        List<RamDTO> rams = ramService.getActive();
        return ResponseEntity.ok(rams);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RamDTO> getById(@PathVariable Integer id) {
        Optional<RamDTO> ram = ramService.getById(id);
        return ram.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RamDTO> create(@RequestBody RamDTO ramDTO) {
        RamDTO created = ramService.save(ramDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RamDTO> update(@PathVariable Integer id, @RequestBody RamDTO ramDTO) {
        try {
            RamDTO updated = ramService.update(id, ramDTO);
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
            ramService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            ramService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}