package com.example.datn_sd28_2025.controller;

import com.example.datn_sd28_2025.dto.SimDTO;
import com.example.datn_sd28_2025.entity.Sim;
import com.example.datn_sd28_2025.repository.SimRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sim")
@CrossOrigin(origins = "*")
public class SimController {

    @Autowired
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
    }
}
