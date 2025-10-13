package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.RomDTO;
import com.example.datn_sd28_2025.service.RomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/rom")
@CrossOrigin(origins = "*")
public class RomController {

    @Autowired
    private RomService romService;

    @GetMapping
    public ResponseEntity<List<RomDTO>> getAll() {
        List<RomDTO> roms = romService.getAll();
        return ResponseEntity.ok(roms);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RomDTO> getById(@PathVariable Integer id) {
        Optional<RomDTO> rom = romService.getById(id);
        return rom.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RomDTO> create(@RequestBody RomDTO romDTO) {
        RomDTO created = romService.save(romDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RomDTO> update(@PathVariable Integer id, @RequestBody RomDTO romDTO) {
        try {
            RomDTO updated = romService.update(id, romDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            romService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}