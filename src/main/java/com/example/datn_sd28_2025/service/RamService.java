package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.RamDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RamService {
    List<RamDTO> getAll();
    Page<RamDTO> getAll(Pageable pageable);
    Optional<RamDTO> getById(Integer id);
    RamDTO save(RamDTO ramDTO);
    void delete(Integer id);
    RamDTO update(Integer id, RamDTO ramDTO);
    void updateStatus(Integer id, Integer trangThai);
}
