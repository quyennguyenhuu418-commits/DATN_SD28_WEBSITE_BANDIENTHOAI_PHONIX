package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.entity.KhuyenMai;
import com.example.datn_sd28_2025.repository.KhuyenMaiRepository;
import com.example.datn_sd28_2025.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Override
    public List<KhuyenMaiDTO> getAll() {
        return khuyenMaiRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<KhuyenMaiDTO> getById(Integer id) {
        return khuyenMaiRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public List<KhuyenMaiDTO> getActivePromotions() {
        return khuyenMaiRepository.findActivePromotions(LocalDateTime.now()).stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<KhuyenMaiDTO> getByCode(String code) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findByMaKhuyenMai(code);
        return khuyenMai != null ? Optional.of(convertToDto(khuyenMai)) : Optional.empty();
    }

    @Override
    public KhuyenMaiDTO save(KhuyenMaiDTO khuyenMaiDTO) {
        KhuyenMai khuyenMai = convertToEntity(khuyenMaiDTO);
        
        // Tự động tạo mã khuyến mãi nếu chưa có
        if (khuyenMai.getMaKhuyenMai() == null || khuyenMai.getMaKhuyenMai().isEmpty()) {
            khuyenMai.setMaKhuyenMai(generatePromotionCode());
        }
        
        khuyenMai.setNgayTao(LocalDateTime.now());
        khuyenMai.setTrangThai(1); // Mặc định là hoạt động
        
        return convertToDto(khuyenMaiRepository.save(khuyenMai));
    }

    @Override
    public KhuyenMaiDTO update(Integer id, KhuyenMaiDTO khuyenMaiDTO) {
        return khuyenMaiRepository.findById(id).map(existingKhuyenMai -> {
            existingKhuyenMai.setTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai());
            existingKhuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
            existingKhuyenMai.setMucDoUuTien(khuyenMaiDTO.getMucDoUuTien());
            existingKhuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
            existingKhuyenMai.setGiamToiDa(khuyenMaiDTO.getGiamToiDa());
            existingKhuyenMai.setNgayBatDau(khuyenMaiDTO.getNgayBatDau());
            existingKhuyenMai.setNgayKetThuc(khuyenMaiDTO.getNgayKetThuc());
            existingKhuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
            existingKhuyenMai.setNgayCapNhat(LocalDateTime.now());
            
            return convertToDto(khuyenMaiRepository.save(existingKhuyenMai));
        }).orElseThrow(() -> new RuntimeException("KhuyenMai not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        if (!khuyenMaiRepository.existsById(id)) {
            throw new RuntimeException("KhuyenMai not found with id " + id);
        }
        khuyenMaiRepository.deleteById(id);
    }

    @Override
    public KhuyenMaiDTO toggleStatus(Integer id) {
        return khuyenMaiRepository.findById(id).map(khuyenMai -> {
            khuyenMai.setTrangThai(khuyenMai.getTrangThai() == 1 ? 0 : 1);
            khuyenMai.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(khuyenMaiRepository.save(khuyenMai));
        }).orElseThrow(() -> new RuntimeException("KhuyenMai not found with id " + id));
    }

    private String generatePromotionCode() {
        return "PROMO" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private KhuyenMaiDTO convertToDto(KhuyenMai khuyenMai) {
        return KhuyenMaiDTO.builder()
                .id(khuyenMai.getId())
                .maKhuyenMai(khuyenMai.getMaKhuyenMai())
                .tenKhuyenMai(khuyenMai.getTenKhuyenMai())
                .moTa(khuyenMai.getMoTa())
                .mucDoUuTien(khuyenMai.getMucDoUuTien())
                .phanTramGiam(khuyenMai.getPhanTramGiam())
                .giamToiDa(khuyenMai.getGiamToiDa())
                .ngayBatDau(khuyenMai.getNgayBatDau())
                .ngayKetThuc(khuyenMai.getNgayKetThuc())
                .ngayTao(khuyenMai.getNgayTao())
                .ngayCapNhat(khuyenMai.getNgayCapNhat())
                .nguoiTao(khuyenMai.getNguoiTao())
                .nguoiCapNhat(khuyenMai.getNguoiCapNhat())
                .trangThai(khuyenMai.getTrangThai())
                .build();
    }

    private KhuyenMai convertToEntity(KhuyenMaiDTO dto) {
        return KhuyenMai.builder()
                .id(dto.getId())
                .maKhuyenMai(dto.getMaKhuyenMai())
                .tenKhuyenMai(dto.getTenKhuyenMai())
                .moTa(dto.getMoTa())
                .mucDoUuTien(dto.getMucDoUuTien())
                .phanTramGiam(dto.getPhanTramGiam())
                .giamToiDa(dto.getGiamToiDa())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .ngayTao(dto.getNgayTao())
                .ngayCapNhat(dto.getNgayCapNhat())
                .nguoiTao(dto.getNguoiTao())
                .nguoiCapNhat(dto.getNguoiCapNhat())
                .trangThai(dto.getTrangThai())
                .build();
    }
}

