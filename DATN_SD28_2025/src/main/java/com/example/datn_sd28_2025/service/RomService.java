package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.RomDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface RomService {
    List<RomDTO> getAll();
    Page<RomDTO> getAll(Pageable pageable);
    Optional<RomDTO> getById(Integer id);
    RomDTO save(RomDTO romDTO);
    void delete(Integer id);
    RomDTO update(Integer id, RomDTO romDTO);
}
