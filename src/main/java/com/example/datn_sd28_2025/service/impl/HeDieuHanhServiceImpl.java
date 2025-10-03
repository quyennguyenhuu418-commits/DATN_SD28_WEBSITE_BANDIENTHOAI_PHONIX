package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.HeDieuHanhDTO;
import com.example.datn_sd28_2025.entity.HeDieuHanh;
import com.example.datn_sd28_2025.repository.HeDieuHanhRepository;
import com.example.datn_sd28_2025.service.HeDieuHanhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HeDieuHanhServiceImpl implements HeDieuHanhService {

    @Autowired
    private HeDieuHanhRepository heDieuHanhRepository;

    @Override
    public List<HeDieuHanhDTO> getAll() {
        return heDieuHanhRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<HeDieuHanhDTO> getById(Integer id) {
        return heDieuHanhRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public HeDieuHanhDTO save(HeDieuHanhDTO heDieuHanhDTO) {
        HeDieuHanh heDieuHanh = convertToEntity(heDieuHanhDTO);
        heDieuHanh.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (heDieuHanh.getTrangThai() == null) {
            heDieuHanh.setTrangThai(1);
        }
        return convertToDto(heDieuHanhRepository.save(heDieuHanh));
    }

    @Override
    public HeDieuHanhDTO update(Integer id, HeDieuHanhDTO heDieuHanhDTO) {
        return heDieuHanhRepository.findById(id).map(existingHeDieuHanh -> {
            existingHeDieuHanh.setMaHeDieuHanh(heDieuHanhDTO.getMaHeDieuHanh());
            existingHeDieuHanh.setTenHeDieuHanh(heDieuHanhDTO.getTenHeDieuHanh());
            existingHeDieuHanh.setMoTa(heDieuHanhDTO.getMoTa());
            existingHeDieuHanh.setTrangThai(heDieuHanhDTO.getTrangThai());
            existingHeDieuHanh.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(heDieuHanhRepository.save(existingHeDieuHanh));
        }).orElseThrow(() -> new RuntimeException("HeDieuHanh not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem heDieuHanh có đang được sử dụng trong sản phẩm không
        HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("HeDieuHanh not found with id " + id));
        
        if (heDieuHanh.getSanPhams() != null && !heDieuHanh.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa hệ điều hành này vì đang được sử dụng trong sản phẩm");
        }
        
        heDieuHanhRepository.deleteById(id);
    }

    private HeDieuHanhDTO convertToDto(HeDieuHanh heDieuHanh) {
        return HeDieuHanhDTO.builder()
                .id(heDieuHanh.getId())
                .maHeDieuHanh(heDieuHanh.getMaHeDieuHanh())
                .tenHeDieuHanh(heDieuHanh.getTenHeDieuHanh())
                .moTa(heDieuHanh.getMoTa())
                .ngayTao(heDieuHanh.getNgayTao())
                .ngayCapNhat(heDieuHanh.getNgayCapNhat())
                .trangThai(heDieuHanh.getTrangThai())
                .build();
    }

    private HeDieuHanh convertToEntity(HeDieuHanhDTO heDieuHanhDTO) {
        return HeDieuHanh.builder()
                .id(heDieuHanhDTO.getId())
                .maHeDieuHanh(heDieuHanhDTO.getMaHeDieuHanh())
                .tenHeDieuHanh(heDieuHanhDTO.getTenHeDieuHanh())
                .moTa(heDieuHanhDTO.getMoTa())
                .ngayTao(heDieuHanhDTO.getNgayTao())
                .ngayCapNhat(heDieuHanhDTO.getNgayCapNhat())
                .trangThai(heDieuHanhDTO.getTrangThai())
                .build();
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        HeDieuHanh heDieuHanh = heDieuHanhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hệ điều hành với ID: " + id));
        heDieuHanh.setTrangThai(trangThai);
        heDieuHanh.setNgayCapNhat(LocalDateTime.now());
        heDieuHanhRepository.save(heDieuHanh);
    }
}
