package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.SimDTO;
import com.example.datn_sd28_2025.entity.Sim;
import com.example.datn_sd28_2025.repository.SimRepository;
import com.example.datn_sd28_2025.service.SimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SimServiceImpl implements SimService {

    @Autowired
    private SimRepository simRepository;

    @Override
    public List<SimDTO> getAll() {
        return simRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<SimDTO> getActive() {
        return simRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Page<SimDTO> getAll(Pageable pageable) {
        return simRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public Optional<SimDTO> getById(Integer id) {
        return simRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public SimDTO save(SimDTO simDTO) {
        Sim sim = convertToEntity(simDTO);
        sim.setNgayTao(LocalDateTime.now());
        if (sim.getTrangThai() == null) {
            sim.setTrangThai(1);
        }
        return convertToDto(simRepository.save(sim));
    }

    @Override
    public SimDTO update(Integer id, SimDTO simDTO) {
        return simRepository.findById(id).map(existingSim -> {
            existingSim.setLoaiSim(simDTO.getLoaiSim());
            existingSim.setMoTa(simDTO.getMoTa());
            existingSim.setTrangThai(simDTO.getTrangThai());
            existingSim.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(simRepository.save(existingSim));
        }).orElseThrow(() -> new RuntimeException("Sim not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        simRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Sim sim = simRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy SIM với ID: " + id));
        sim.setTrangThai(trangThai);
        sim.setNgayCapNhat(LocalDateTime.now());
        simRepository.save(sim);
    }

    private SimDTO convertToDto(Sim sim) {
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



