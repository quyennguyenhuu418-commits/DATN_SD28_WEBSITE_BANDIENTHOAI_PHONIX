package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CpuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CpuService {
    List<CpuDTO> getAll();
    Page<CpuDTO> getAll(Pageable pageable);
    Optional<CpuDTO> getById(Integer id);
    CpuDTO save(CpuDTO cpuDTO);
    CpuDTO update(Integer id, CpuDTO cpuDTO);
    void delete(Integer id);
}
