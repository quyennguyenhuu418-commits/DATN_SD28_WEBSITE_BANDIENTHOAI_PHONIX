package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.Imei;  

import java.util.List;

public interface ImeiService {
    Imei create(Imei imei);
    List<Imei> bulkCreate(Integer chiTietId, List<String> imeis);
    List<Imei> findByChiTiet(Integer chiTietId, Integer status);
    Imei updateStatus(Integer id, Integer status);
    
    // DTO methods
    List<ImeiDTO> getAll();
    ImeiDTO getById(Integer id);
    ImeiDTO save(ImeiDTO imeiDTO);
    ImeiDTO update(Integer id, ImeiDTO imeiDTO);
    void delete(Integer id);
    
    // Check for duplicate IMEIs
    List<String> findDuplicateImeis(List<String> imeis);
    
    // Get IMEIs by ChiTietSanPham ID
    List<ImeiDTO> getByChiTietSanPham(Integer chiTietSanPhamId);
}


