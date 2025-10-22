package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.DiaChiDTO;

import java.util.List;

public interface DiaChiService {
    List<DiaChiDTO> getAll();
    DiaChiDTO getById(Integer id);
    DiaChiDTO create(DiaChiDTO diaChiDTO);
    DiaChiDTO update(Integer id, DiaChiDTO diaChiDTO);
    void delete(Integer id);
}





