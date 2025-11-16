package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.DiaChiDTO;
import com.example.datn_sd28_2025.service.DiaChiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaChiController {
    
    private final DiaChiService diaChiService;
    
    @GetMapping
    public ResponseEntity<List<DiaChiDTO>> getAll() {
        try {
            List<DiaChiDTO> diaChis = diaChiService.getAll();
            return ResponseEntity.ok(diaChis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DiaChiDTO> getById(@PathVariable Integer id) {
        try {
            DiaChiDTO diaChi = diaChiService.getById(id);
            return ResponseEntity.ok(diaChi);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping
    public ResponseEntity<DiaChiDTO> create(@RequestBody DiaChiDTO diaChiDTO) {
        try {
            DiaChiDTO createdDiaChi = diaChiService.create(diaChiDTO);
            return ResponseEntity.ok(createdDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DiaChiDTO> update(@PathVariable Integer id, @RequestBody DiaChiDTO diaChiDTO) {
        try {
            DiaChiDTO updatedDiaChi = diaChiService.update(id, diaChiDTO);
            return ResponseEntity.ok(updatedDiaChi);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            diaChiService.delete(id);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}





