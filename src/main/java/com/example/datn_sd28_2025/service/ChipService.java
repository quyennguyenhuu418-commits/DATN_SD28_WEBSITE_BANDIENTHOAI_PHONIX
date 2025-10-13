package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ChipDTO;


import java.util.List;
import java.util.Optional;

public interface ChipService {
    List<ChipDTO> getAll();
    List<ChipDTO> getActive();
    Optional<ChipDTO> getById(Integer id);
    ChipDTO save(ChipDTO chipDTO);
    void delete(Integer id);
    ChipDTO update(Integer id, ChipDTO chipDTO);
    void updateStatus(Integer id, Integer trangThai);
}
