package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.KhachHangDTO;
import com.example.datn_sd28_2025.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/khach-hang")
@CrossOrigin(origins = "*")
public class KhachHangController {

    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public ResponseEntity<List<KhachHangDTO>> getAll() {
        List<KhachHangDTO> khachHangs = khachHangService.getAll();
        return ResponseEntity.ok(khachHangs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<KhachHangDTO> getById(@PathVariable Integer id) {
        Optional<KhachHangDTO> khachHang = khachHangService.getById(id);
        return khachHang.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<KhachHangDTO>> searchByQuery(@RequestParam String query) {
        List<KhachHangDTO> khachHangs = khachHangService.searchByQuery(query);
        return ResponseEntity.ok(khachHangs);
    }

    @PostMapping
    public ResponseEntity<KhachHangDTO> create(@RequestBody KhachHangDTO khachHangDTO) {
        try {
            KhachHangDTO created = khachHangService.save(khachHangDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<KhachHangDTO> update(@PathVariable Integer id, @RequestBody KhachHangDTO khachHangDTO) {
        try {
            KhachHangDTO updated = khachHangService.update(id, khachHangDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            khachHangService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/count/active")
    public ResponseEntity<Long> countActiveCustomers() {
        Long count = khachHangService.countActiveCustomers();
        return ResponseEntity.ok(count);
    }
}



