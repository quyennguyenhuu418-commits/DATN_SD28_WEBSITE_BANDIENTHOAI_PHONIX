package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.DanhMucDTO;

import java.util.List;
import java.util.Optional;

public interface DanhMucService {
    List<DanhMucDTO> getAll();
    List<DanhMucDTO> getActive();
    Optional<DanhMucDTO> getById(Integer id);
    DanhMucDTO save(DanhMucDTO danhMucDTO);
    void delete(Integer id);
    DanhMucDTO update(Integer id, DanhMucDTO danhMucDTO);
<<<<<<< HEAD
    void updateStatus(Integer id, Integer trangThai);
=======
>>>>>>> origin/Huan
}
