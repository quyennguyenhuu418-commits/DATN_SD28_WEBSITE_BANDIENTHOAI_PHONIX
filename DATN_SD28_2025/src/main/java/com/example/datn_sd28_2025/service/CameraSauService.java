package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CameraSauDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CameraSauService {
    List<CameraSauDTO> getAll();
    Optional<CameraSauDTO> getById(Integer id);
    CameraSauDTO save(CameraSauDTO cameraSauDTO);
    void delete(Integer id);
    CameraSauDTO update(Integer id, CameraSauDTO cameraSauDTO);
}
