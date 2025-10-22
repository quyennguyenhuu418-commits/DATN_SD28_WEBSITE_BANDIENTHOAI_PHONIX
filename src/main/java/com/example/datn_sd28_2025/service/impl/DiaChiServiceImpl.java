package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.DiaChiDTO;
import com.example.datn_sd28_2025.entity.DiaChi;
import com.example.datn_sd28_2025.repository.DiaChiRepository;
import com.example.datn_sd28_2025.service.DiaChiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaChiServiceImpl implements DiaChiService {
    
    private final DiaChiRepository diaChiRepository;
    
    @Override
    public List<DiaChiDTO> getAll() {
        return diaChiRepository.findAllActive()
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    @Override
    public DiaChiDTO getById(Integer id) {
        DiaChi diaChi = diaChiRepository.findActiveById(id);
        if (diaChi == null) {
            throw new RuntimeException("Địa chỉ không tồn tại với id: " + id);
        }
        return convertToDto(diaChi);
    }
    
    @Override
    @Transactional
    public DiaChiDTO create(DiaChiDTO diaChiDTO) {
        DiaChi diaChi = convertToEntity(diaChiDTO);
        diaChi.setNgayTao(LocalDateTime.now());
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChi.setTrangThai(1);
        
        return convertToDto(diaChiRepository.save(diaChi));
    }
    
    @Override
    @Transactional
    public DiaChiDTO update(Integer id, DiaChiDTO diaChiDTO) {
        return diaChiRepository.findById(id)
                .map(existingDiaChi -> {
                    existingDiaChi.setDiaChiChiTiet(diaChiDTO.getDiaChiChiTiet());
                    existingDiaChi.setPhuongXa(diaChiDTO.getPhuongXa());
                    existingDiaChi.setQuanHuyen(diaChiDTO.getQuanHuyen());
                    existingDiaChi.setTinhThanhPho(diaChiDTO.getTinhThanhPho());
                    existingDiaChi.setMaBuuDien(diaChiDTO.getMaBuuDien());
                    existingDiaChi.setGhiChu(diaChiDTO.getGhiChu());
                    existingDiaChi.setNgayCapNhat(LocalDateTime.now());
                    
                    return convertToDto(diaChiRepository.save(existingDiaChi));
                })
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với id: " + id));
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        DiaChi diaChi = diaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với id: " + id));
        
        diaChi.setTrangThai(0);
        diaChi.setNgayCapNhat(LocalDateTime.now());
        diaChiRepository.save(diaChi);
    }
    
    private DiaChiDTO convertToDto(DiaChi diaChi) {
        return DiaChiDTO.builder()
                .id(diaChi.getId())
                .diaChiChiTiet(diaChi.getDiaChiChiTiet())
                .phuongXa(diaChi.getPhuongXa())
                .quanHuyen(diaChi.getQuanHuyen())
                .tinhThanhPho(diaChi.getTinhThanhPho())
                .maBuuDien(diaChi.getMaBuuDien())
                .ghiChu(diaChi.getGhiChu())
                .ngayTao(diaChi.getNgayTao())
                .ngayCapNhat(diaChi.getNgayCapNhat())
                .trangThai(diaChi.getTrangThai())
                .build();
    }
    
    private DiaChi convertToEntity(DiaChiDTO diaChiDTO) {
        return DiaChi.builder()
                .id(diaChiDTO.getId())
                .diaChiChiTiet(diaChiDTO.getDiaChiChiTiet())
                .phuongXa(diaChiDTO.getPhuongXa())
                .quanHuyen(diaChiDTO.getQuanHuyen())
                .tinhThanhPho(diaChiDTO.getTinhThanhPho())
                .maBuuDien(diaChiDTO.getMaBuuDien())
                .ghiChu(diaChiDTO.getGhiChu())
                .ngayTao(diaChiDTO.getNgayTao())
                .ngayCapNhat(diaChiDTO.getNgayCapNhat())
                .trangThai(diaChiDTO.getTrangThai())
                .build();
    }
}





