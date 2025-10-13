package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;

import java.util.List;
import java.util.Optional;

public interface KhuyenMaiService {
    List<KhuyenMaiDTO> getAll();
    Optional<KhuyenMaiDTO> getById(Integer id);
    List<KhuyenMaiDTO> getActivePromotions();
    Optional<KhuyenMaiDTO> getByCode(String code);
    KhuyenMaiDTO save(KhuyenMaiDTO khuyenMaiDTO);
    KhuyenMaiDTO update(Integer id, KhuyenMaiDTO khuyenMaiDTO);
    void delete(Integer id);
    KhuyenMaiDTO toggleStatus(Integer id);
}

