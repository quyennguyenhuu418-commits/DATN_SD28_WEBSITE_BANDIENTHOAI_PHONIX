package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CameraTruocDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CameraTruocService {
    List<CameraTruocDTO> getAll();
    Optional<CameraTruocDTO> getById(Integer id);
    CameraTruocDTO save(CameraTruocDTO cameraTruocDTO);
    void delete(Integer id);
    CameraTruocDTO update(Integer id, CameraTruocDTO cameraTruocDTO);
}
