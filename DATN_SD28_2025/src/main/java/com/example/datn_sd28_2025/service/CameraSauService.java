package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CameraSauDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CameraSauService {
    List<CameraSauDTO> getAll();
    List<CameraSauDTO> getActive();
    Optional<CameraSauDTO> getById(Integer id);
    CameraSauDTO save(CameraSauDTO cameraSauDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
    CameraSauDTO update(Integer id, CameraSauDTO cameraSauDTO);
}
