package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.GpuDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GpuService {
    List<GpuDTO> getAll();
    Page<GpuDTO> getAll(Pageable pageable);
    Optional<GpuDTO> getById(Integer id);
    GpuDTO save(GpuDTO gpuDTO);
    GpuDTO update(Integer id, GpuDTO gpuDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
}
