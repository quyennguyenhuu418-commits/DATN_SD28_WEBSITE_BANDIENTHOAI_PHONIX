package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ImeiDaBanDTO;

import java.util.List;

public interface ImeiDaBanService {
    ImeiDaBanDTO createImeiDaBan(ImeiDaBanDTO imeiDaBanDTO);
    List<ImeiDaBanDTO> getByHoaDonChiTiet(Integer idHoaDonChiTiet);
    ImeiDaBanDTO getByImei(String imei);
    List<ImeiDaBanDTO> getAllActive();
    void markImeiAsSold(String imei, Integer idHoaDonChiTiet);
}

