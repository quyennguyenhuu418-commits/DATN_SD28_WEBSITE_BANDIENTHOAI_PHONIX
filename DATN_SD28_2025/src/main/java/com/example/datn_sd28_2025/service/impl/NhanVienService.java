package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.NhanVienDTO;

import java.util.List;
import java.util.Optional;

public interface NhanVienService {
    List<NhanVienDTO> getAll();
    Optional<NhanVienDTO> getById(Integer id);
    NhanVienDTO save(NhanVienDTO nhanVienDTO);
    NhanVienDTO update(Integer id, NhanVienDTO nhanVienDTO);
    void delete(Integer id);
    List<NhanVienDTO> searchByQuery(String query);
    List<NhanVienDTO> findByChucVu(String chucVu);
    List<NhanVienDTO> findAllActive();
    boolean existsByCccd(String cccd);
}





