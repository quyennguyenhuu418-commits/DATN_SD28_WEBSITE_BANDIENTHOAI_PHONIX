package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.DanhMucDTO;
import com.example.datn_sd28_2025.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/danh-muc")
@CrossOrigin(origins = "*")
public class DanhMucController {

    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    public ResponseEntity<List<DanhMucDTO>> getAll() {
        List<DanhMucDTO> danhMucs = danhMucService.getAll();
        return ResponseEntity.ok(danhMucs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMucDTO> getById(@PathVariable Integer id) {
        Optional<DanhMucDTO> danhMuc = danhMucService.getById(id);
        return danhMuc.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<DanhMucDTO> create(@RequestBody DanhMucDTO danhMucDTO) {
        DanhMucDTO created = danhMucService.save(danhMucDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DanhMucDTO> update(@PathVariable Integer id, @RequestBody DanhMucDTO danhMucDTO) {
        try {
            DanhMucDTO updated = danhMucService.update(id, danhMucDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            danhMucService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}