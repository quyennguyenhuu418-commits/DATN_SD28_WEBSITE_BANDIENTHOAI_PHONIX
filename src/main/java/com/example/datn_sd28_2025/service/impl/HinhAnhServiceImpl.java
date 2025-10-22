package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.entity.HinhAnh;
import com.example.datn_sd28_2025.repository.HinhAnhRepository;
import com.example.datn_sd28_2025.service.HinhAnhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class HinhAnhServiceImpl implements HinhAnhService {

    @Autowired
    private HinhAnhRepository hinhAnhRepository;

    @Override
    public List<HinhAnh> getByChiTietSanPhamId(Integer chiTietSanPhamId) {
        return hinhAnhRepository.findByChiTietSanPhamId(chiTietSanPhamId);
    }

    @Override
    public HinhAnh save(HinhAnh hinhAnh) {
        if (hinhAnh.getNgayTao() == null) {
            hinhAnh.setNgayTao(LocalDateTime.now());
        }
        hinhAnh.setNgaySua(LocalDateTime.now());
        if (hinhAnh.getTrangThai() == null) {
            hinhAnh.setTrangThai(1);
        }
        return hinhAnhRepository.save(hinhAnh);
    }

    @Override
    public void delete(Integer id) {
        hinhAnhRepository.deleteById(id);
    }

    @Override
    public void deleteByChiTietSanPhamId(Integer chiTietSanPhamId) {
        List<HinhAnh> hinhAnhs = hinhAnhRepository.findByChiTietSanPhamId(chiTietSanPhamId);
        hinhAnhRepository.deleteAll(hinhAnhs);
    }
}
