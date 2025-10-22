package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SanPhamPosDTO;

import java.util.List;

public interface SanPhamPosService {
    
    List<SanPhamPosDTO> getAllProductsForPos();
    
    List<SanPhamPosDTO> getProductsByHang(Integer hangId);
    
    List<SanPhamPosDTO> searchProducts(String keyword);
    
    SanPhamPosDTO getProductById(Integer id);
    
    boolean updateStock(Integer chiTietSanPhamId, Integer quantity);
    
    boolean checkStock(Integer chiTietSanPhamId, Integer quantity);
    
    // Debug methods
    java.util.Map<String, Object> getDatabaseStats();
}
