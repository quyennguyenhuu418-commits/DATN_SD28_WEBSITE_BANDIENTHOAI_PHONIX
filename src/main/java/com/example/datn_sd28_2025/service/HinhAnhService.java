package com.example.datn_sd28_2025.service;


import com.example.datn_sd28_2025.entity.HinhAnh;

import java.util.List;


public interface HinhAnhService {
    List<HinhAnh> getByChiTietSanPhamId(Integer chiTietSanPhamId);
    HinhAnh save(HinhAnh hinhAnh);
    void delete(Integer id);
    void deleteByChiTietSanPhamId(Integer chiTietSanPhamId);
}
