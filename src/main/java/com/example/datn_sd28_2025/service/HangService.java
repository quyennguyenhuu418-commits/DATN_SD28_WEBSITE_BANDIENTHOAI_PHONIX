package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.HangDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface HangService {
    List<HangDTO> getAll();
    Optional<HangDTO> getById(Integer id);
    HangDTO save(HangDTO hangDTO);
    void delete(Integer id);
    HangDTO update(Integer id, HangDTO hangDTO);
    void updateStatus(Integer id, Integer trangThai);
}
