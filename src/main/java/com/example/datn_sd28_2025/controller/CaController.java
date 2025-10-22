package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.CaDTO;
import com.example.datn_sd28_2025.service.CaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/ca")
@CrossOrigin(origins = "*")
public class CaController {

    @Autowired
    private CaService caService;

    @GetMapping
    public ResponseEntity<List<CaDTO>> getAll() {
        try {
            List<CaDTO> caList = caService.getAll();
            return ResponseEntity.ok(caList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/active")
    public ResponseEntity<List<CaDTO>> getActiveCa() {
        try {
            List<CaDTO> caList = caService.getActiveCa();
            return ResponseEntity.ok(caList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/current")
    public ResponseEntity<List<CaDTO>> getCurrentActiveCa() {
        try {
            List<CaDTO> caList = caService.getCurrentActiveCa();
            return ResponseEntity.ok(caList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<CaDTO> getById(@PathVariable Integer id) {
        try {
            Optional<CaDTO> ca = caService.getById(id);
            return ca.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<CaDTO> create(@Valid @RequestBody CaDTO caDTO) {
        try {
            CaDTO createdCa = caService.save(caDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<CaDTO> update(@PathVariable Integer id, @Valid @RequestBody CaDTO caDTO) {
        try {
            CaDTO updatedCa = caService.update(id, caDTO);
            return ResponseEntity.ok(updatedCa);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            caService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<CaDTO>> searchByTenCa(@RequestParam String tenCa) {
        try {
            List<CaDTO> caList = caService.searchByTenCa(tenCa);
            return ResponseEntity.ok(caList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/exists/ma-ca/{maCa}")
    public ResponseEntity<Boolean> existsByMaCa(@PathVariable String maCa) {
        try {
            boolean exists = caService.existsByMaCa(maCa);
            return ResponseEntity.ok(exists);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

























