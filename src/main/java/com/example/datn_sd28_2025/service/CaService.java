package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CaDTO;

import java.util.List;
import java.util.Optional;

public interface CaService {
    List<CaDTO> getAll();
    Optional<CaDTO> getById(Integer id);
    CaDTO save(CaDTO caDTO);
    CaDTO update(Integer id, CaDTO caDTO);
    void delete(Integer id);
    List<CaDTO> getActiveCa();
    List<CaDTO> getCurrentActiveCa();
    List<CaDTO> searchByTenCa(String tenCa);
    boolean existsByMaCa(String maCa);
    boolean existsByMaCaAndIdNot(String maCa, Integer id);
}

























