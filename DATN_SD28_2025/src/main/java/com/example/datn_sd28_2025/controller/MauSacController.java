package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.MauSacDTO;
import com.example.datn_sd28_2025.service.MauSacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mau-sac")
@CrossOrigin(origins = "*")
public class MauSacController {

    @Autowired
    private MauSacService mauSacService;

    @GetMapping
    public ResponseEntity<List<MauSacDTO>> getAll() {
        List<MauSacDTO> mauSacs = mauSacService.getAll();
        return ResponseEntity.ok(mauSacs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MauSacDTO> getById(@PathVariable Integer id) {
        Optional<MauSacDTO> mauSac = mauSacService.getById(id);
        return mauSac.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<MauSacDTO> create(@RequestBody MauSacDTO mauSacDTO) {
        MauSacDTO created = mauSacService.save(mauSacDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MauSacDTO> update(@PathVariable Integer id, @RequestBody MauSacDTO mauSacDTO) {
        try {
            MauSacDTO updated = mauSacService.update(id, mauSacDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            mauSacService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}