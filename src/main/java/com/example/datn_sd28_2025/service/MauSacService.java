package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.MauSacDTO;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======

>>>>>>> origin/Huan

import java.util.List;
import java.util.Optional;

public interface MauSacService {
    List<MauSacDTO> getAll();
    List<MauSacDTO> getActive();
    Optional<MauSacDTO> getById(Integer id);
    MauSacDTO save(MauSacDTO mauSacDTO);
    void delete(Integer id);
    void updateStatus(Integer id, Integer trangThai);
    MauSacDTO update(Integer id, MauSacDTO mauSacDTO);
}
