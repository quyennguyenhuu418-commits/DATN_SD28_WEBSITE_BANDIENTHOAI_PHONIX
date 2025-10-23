package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.HeDieuHanhDTO;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======

>>>>>>> origin/Huan

import java.util.List;
import java.util.Optional;

public interface HeDieuHanhService {
    List<HeDieuHanhDTO> getAll();
    List<HeDieuHanhDTO> getActive();
    Optional<HeDieuHanhDTO> getById(Integer id);
    HeDieuHanhDTO save(HeDieuHanhDTO heDieuHanhDTO);
    void delete(Integer id);
    HeDieuHanhDTO update(Integer id, HeDieuHanhDTO heDieuHanhDTO);
    void updateStatus(Integer id, Integer trangThai);
}
