package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.DotGiamGiaSanPhamDTO;
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

    // Quản lý danh sách sản phẩm áp dụng đợt giảm giá
    List<DotGiamGiaSanPhamDTO> getAppliedProducts(Integer idKhuyenMai);
    DotGiamGiaSanPhamDTO addProductToPromotion(Integer idKhuyenMai, Integer idSanPham, String nguoiTao);
    void removeProductFromPromotion(Integer idKhuyenMai, Integer idSanPham);
    void addMultipleProducts(Integer idKhuyenMai, List<Integer> idSanPhams, String nguoiTao);
    long countAppliedProducts(Integer idKhuyenMai);
}

