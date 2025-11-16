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
    DotGiamGiaSanPhamDTO addProductToPromotion(Integer idKhuyenMai, Integer idChiTietSanPham, String nguoiTao);
    void removeProductFromPromotion(Integer idKhuyenMai, Integer idChiTietSanPham);
    void addMultipleProducts(Integer idKhuyenMai, List<Integer> idChiTietSanPhams, String nguoiTao);
    void addMultipleProductsWithQuantity(Integer idKhuyenMai, List<com.example.datn_sd28_2025.controller.KhuyenMaiController.ProductQuantityData> products, String nguoiTao);
    long countAppliedProducts(Integer idKhuyenMai);
}

