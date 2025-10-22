package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.UserDiaChiDTO;

import java.util.List;

public interface UserDiaChiService {
    List<UserDiaChiDTO> getByKhachHangId(Integer idKhachHang);
    UserDiaChiDTO getDiaChiMacDinhByKhachHangId(Integer idKhachHang);
    List<UserDiaChiDTO> getByKhachHangIdAndLoaiDiaChi(Integer idKhachHang, String loaiDiaChi);
    UserDiaChiDTO create(UserDiaChiDTO userDiaChiDTO);
    UserDiaChiDTO update(Integer id, UserDiaChiDTO userDiaChiDTO);
    void delete(Integer id);
    UserDiaChiDTO setDiaChiMacDinh(Integer id);
}

