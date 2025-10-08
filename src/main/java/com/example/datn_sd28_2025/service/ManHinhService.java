package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ManHinhDTO;

import java.util.List;
import java.util.Optional;

public interface ManHinhService {
    List<ManHinhDTO> getAll();
    List<ManHinhDTO> getActive();
    Optional<ManHinhDTO> getById(Integer id);
    ManHinhDTO save(ManHinhDTO manHinhDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
    ManHinhDTO update(Integer id, ManHinhDTO manHinhDTO);
}
