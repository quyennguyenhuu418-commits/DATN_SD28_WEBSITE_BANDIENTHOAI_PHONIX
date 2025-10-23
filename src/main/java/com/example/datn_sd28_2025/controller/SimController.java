package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.SimDTO;
<<<<<<< HEAD
import com.example.datn_sd28_2025.service.SimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
=======
import com.example.datn_sd28_2025.entity.Sim;
import com.example.datn_sd28_2025.repository.SimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
>>>>>>> origin/Huan

@RestController
@RequestMapping("/api/sim")
@CrossOrigin(origins = "*")
public class SimController {

    @Autowired
<<<<<<< HEAD
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
=======
    private SimRepository simRepository;

    @GetMapping
    public ResponseEntity<List<SimDTO>> getAllSims() {
        List<Sim> sims = simRepository.findAll();
        List<SimDTO> simDTOs = sims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(simDTOs);
    }

    @GetMapping("/active")
    public ResponseEntity<List<SimDTO>> getActiveSims() {
        List<Sim> sims = simRepository.findAllActive();
        List<SimDTO> simDTOs = sims.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(simDTOs);
    }

    @PostMapping
    public ResponseEntity<SimDTO> createSim(@RequestBody SimDTO simDTO) {
        Sim sim = convertToEntity(simDTO);
        sim.setNgayTao(LocalDateTime.now());
        sim.setNgayCapNhat(LocalDateTime.now());
        sim.setTrangThai(1);
        
        Sim savedSim = simRepository.save(sim);
        return ResponseEntity.ok(convertToDTO(savedSim));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SimDTO> updateSim(@PathVariable Integer id, @RequestBody SimDTO simDTO) {
        return simRepository.findById(id)
                .map(existingSim -> {
                    existingSim.setLoaiSim(simDTO.getLoaiSim());
                    existingSim.setMoTa(simDTO.getMoTa());
                    existingSim.setNgayCapNhat(LocalDateTime.now());
                    existingSim.setTrangThai(simDTO.getTrangThai());
                    
                    Sim updatedSim = simRepository.save(existingSim);
                    return ResponseEntity.ok(convertToDTO(updatedSim));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSim(@PathVariable Integer id) {
        return simRepository.findById(id)
                .map(sim -> {
                    simRepository.delete(sim);
                    return ResponseEntity.ok().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private SimDTO convertToDTO(Sim sim) {
        return SimDTO.builder()
                .id(sim.getId())
                .loaiSim(sim.getLoaiSim())
                .moTa(sim.getMoTa())
                .ngayTao(sim.getNgayTao())
                .ngayCapNhat(sim.getNgayCapNhat())
                .trangThai(sim.getTrangThai())
                .build();
    }

    private Sim convertToEntity(SimDTO simDTO) {
        return Sim.builder()
                .id(simDTO.getId())
                .loaiSim(simDTO.getLoaiSim())
                .moTa(simDTO.getMoTa())
                .ngayTao(simDTO.getNgayTao())
                .ngayCapNhat(simDTO.getNgayCapNhat())
                .trangThai(simDTO.getTrangThai())
                .build();
>>>>>>> origin/Huan
    }
}
