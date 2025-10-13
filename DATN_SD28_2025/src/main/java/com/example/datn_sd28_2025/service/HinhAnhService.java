package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.HinhAnhDTO;
import com.example.datn_sd28_2025.entity.HinhAnh;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface HinhAnhService {
    List<HinhAnh> getByChiTietSanPhamId(Integer chiTietSanPhamId);
    HinhAnh save(HinhAnh hinhAnh);
    void delete(Integer id);
    void deleteByChiTietSanPhamId(Integer chiTietSanPhamId);
}
