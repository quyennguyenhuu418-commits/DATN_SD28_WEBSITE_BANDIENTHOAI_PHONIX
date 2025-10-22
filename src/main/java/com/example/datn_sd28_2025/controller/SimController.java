package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.SimDTO;
import com.example.datn_sd28_2025.service.SimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/sim")
@CrossOrigin(origins = "*")
public class SimController {

    @Autowired
    private SimService simService;

    @GetMapping
    public ResponseEntity<List<SimDTO>> getAll() {
        return ResponseEntity.ok(simService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<SimDTO>> getActive() {
        return ResponseEntity.ok(simService.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SimDTO> getById(@PathVariable Integer id) {
        Optional<SimDTO> simDTO = simService.getById(id);
        return simDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SimDTO> create(@RequestBody SimDTO simDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(simService.save(simDTO));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimDTO> update(@PathVariable Integer id, @RequestBody SimDTO simDTO) {
        try {
            SimDTO updatedSim = simService.update(id, simDTO);
            return ResponseEntity.ok(updatedSim);
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
            simService.updateStatus(id, trangThai);
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            simService.delete(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
