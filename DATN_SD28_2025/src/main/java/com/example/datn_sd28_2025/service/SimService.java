package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SimDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface SimService {
    List<SimDTO> getAll();
    List<SimDTO> getActive();
    Page<SimDTO> getAll(Pageable pageable);
    Optional<SimDTO> getById(Integer id);
    SimDTO save(SimDTO simDTO);
    SimDTO update(Integer id, SimDTO simDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
}



