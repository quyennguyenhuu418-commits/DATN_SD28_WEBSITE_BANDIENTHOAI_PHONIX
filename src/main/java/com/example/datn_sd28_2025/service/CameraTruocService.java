package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.CameraTruocDTO;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======

>>>>>>> origin/Huan

import java.util.List;
import java.util.Optional;

public interface CameraTruocService {
    List<CameraTruocDTO> getAll();
    List<CameraTruocDTO> getActive();
    Optional<CameraTruocDTO> getById(Integer id);
    CameraTruocDTO save(CameraTruocDTO cameraTruocDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
    CameraTruocDTO update(Integer id, CameraTruocDTO cameraTruocDTO);
}
