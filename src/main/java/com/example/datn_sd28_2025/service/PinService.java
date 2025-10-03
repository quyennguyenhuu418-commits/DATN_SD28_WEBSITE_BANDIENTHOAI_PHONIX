package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.PinDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface PinService {
    List<PinDTO> getAll();
    Page<PinDTO> getAll(Pageable pageable);
    Optional<PinDTO> getById(Integer id);
    PinDTO save(PinDTO pinDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
    PinDTO update(Integer id, PinDTO pinDTO);
}
