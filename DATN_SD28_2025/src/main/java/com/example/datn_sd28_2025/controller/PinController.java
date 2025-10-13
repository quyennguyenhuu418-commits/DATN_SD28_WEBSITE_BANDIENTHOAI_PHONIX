package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.PinDTO;
import com.example.datn_sd28_2025.service.PinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pin")
@CrossOrigin(origins = "*")
public class PinController {

    @Autowired
    private PinService pinService;

    @GetMapping
    public ResponseEntity<List<PinDTO>> getAll() {
        return ResponseEntity.ok(pinService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PinDTO> getById(@PathVariable Integer id) {
        return pinService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PinDTO> create(@RequestBody PinDTO pinDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pinService.save(pinDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PinDTO> update(@PathVariable Integer id, @RequestBody PinDTO pinDTO) {
        try {
            return ResponseEntity.ok(pinService.update(id, pinDTO));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            pinService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
