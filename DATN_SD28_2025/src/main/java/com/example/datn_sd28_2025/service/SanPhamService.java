package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.dto.SanPhamEditDTO;
import com.example.datn_sd28_2025.dto.SanPhamViewDTO;
import com.example.datn_sd28_2025.dto.ChiTietSanPhamCreateDTO;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.service.dto.SanPhamFullRequest;

import java.util.List;
import java.util.Optional;

public interface SanPhamService {
    List<SanPhamDTO> getAllSanPham();
    List<SanPhamDTO> getActiveSanPham();
    List<SanPhamDTO> searchSanPham(String searchTerm);
    Optional<SanPhamDTO> getSanPhamById(Integer id);
    Optional<SanPhamDTO> getSanPhamByMa(String maSanPham);
    SanPham createSanPham(SanPham sanPham);
    SanPham updateSanPham(Integer id, SanPham sanPham);
    void deleteSanPham(Integer id);
    List<SanPhamDTO> getSanPhamByDanhMuc(Integer danhMucId);
    List<SanPhamDTO> getSanPhamByHang(Integer hangId);
    
    // Chi tiết sản phẩm
    List<ChiTietSanPham> getChiTietSanPhamBySanPhamId(Integer sanPhamId);
    List<ChiTietSanPham> getAvailableChiTietSanPham();
    Optional<ChiTietSanPham> getChiTietSanPhamById(Integer id);
    ChiTietSanPham createChiTietSanPham(ChiTietSanPham chiTietSanPham);
    ChiTietSanPham createChiTietSanPhamFromDTO(ChiTietSanPhamCreateDTO dto);
    ChiTietSanPham updateChiTietSanPham(Integer id, ChiTietSanPham chiTietSanPham);
    void deleteChiTietSanPham(Integer id);

    // Batch create product with variants
    SanPhamDTO createSanPhamFull(SanPhamFullRequest request);
    
    // Update product with variants
    SanPhamDTO updateSanPhamFull(Integer id, SanPhamFullRequest request);
    
    // Get product with full details for editing
    Optional<SanPhamEditDTO> getSanPhamForEdit(Integer id);
    
    // Get product with full details for viewing
    Optional<SanPhamViewDTO> getSanPhamForView(Integer id);
    
    // Cleanup null data
    int cleanupNullData();
    
    // Image management for chi tiet san pham
    HinhAnh addImageToChiTiet(Integer chiTietId, HinhAnh hinhAnh);
    List<HinhAnh> getImagesByChiTietId(Integer chiTietId);
    void deleteAllImagesFromChiTiet(Integer chiTietId);
    
    // Update product status
    void updateStatus(Integer id, Integer trangThai);
}
