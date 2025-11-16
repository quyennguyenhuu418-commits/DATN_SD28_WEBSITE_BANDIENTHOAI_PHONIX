package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.DanhMucDTO;
import com.example.datn_sd28_2025.entity.DanhMuc;
import com.example.datn_sd28_2025.repository.DanhMucRepository;
import com.example.datn_sd28_2025.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class DanhMucServiceImpl implements DanhMucService {

    @Autowired
    private DanhMucRepository danhMucRepository;

    @Override
    public List<DanhMucDTO> getAll() {
        return danhMucRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<DanhMucDTO> getActive() {
        return danhMucRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<DanhMucDTO> getById(Integer id) {
        return danhMucRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public DanhMucDTO save(DanhMucDTO danhMucDTO) {
        DanhMuc danhMuc = convertToEntity(danhMucDTO);
        danhMuc.setNgayTao(LocalDateTime.now());
        danhMuc.setTrangThai(1);
        return convertToDto(danhMucRepository.save(danhMuc));
    }

    @Override
    public DanhMucDTO update(Integer id, DanhMucDTO danhMucDTO) {
        return danhMucRepository.findById(id).map(existingDanhMuc -> {
            existingDanhMuc.setMaDanhMuc(danhMucDTO.getMaDanhMuc());
            existingDanhMuc.setTenDanhMuc(danhMucDTO.getTenDanhMuc());
            existingDanhMuc.setTrangThai(danhMucDTO.getTrangThai());
            existingDanhMuc.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(danhMucRepository.save(existingDanhMuc));
        }).orElseThrow(() -> new RuntimeException("DanhMuc not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem danhMuc có đang được sử dụng trong sản phẩm không
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("DanhMuc not found with id " + id));
        
        if (danhMuc.getSanPhams() != null && !danhMuc.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa danh mục này vì đang được sử dụng trong sản phẩm");
        }
        
        danhMucRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        DanhMuc danhMuc = danhMucRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + id));
        danhMuc.setTrangThai(trangThai);
        danhMuc.setNgayCapNhat(LocalDateTime.now());
        danhMucRepository.save(danhMuc);
    }

    private DanhMucDTO convertToDto(DanhMuc danhMuc) {
        return DanhMucDTO.builder()
                .id(danhMuc.getId())
                .maDanhMuc(danhMuc.getMaDanhMuc())
                .tenDanhMuc(danhMuc.getTenDanhMuc())
                .ngayTao(danhMuc.getNgayTao())
                .ngayCapNhat(danhMuc.getNgayCapNhat())
                .trangThai(danhMuc.getTrangThai())
                .build();
    }

    private DanhMuc convertToEntity(DanhMucDTO danhMucDTO) {
        return DanhMuc.builder()
                .id(danhMucDTO.getId())
                .maDanhMuc(danhMucDTO.getMaDanhMuc())
                .tenDanhMuc(danhMucDTO.getTenDanhMuc())
                .ngayTao(danhMucDTO.getNgayTao())
                .ngayCapNhat(danhMucDTO.getNgayCapNhat())
                .trangThai(danhMucDTO.getTrangThai())
                .build();
    }
}
