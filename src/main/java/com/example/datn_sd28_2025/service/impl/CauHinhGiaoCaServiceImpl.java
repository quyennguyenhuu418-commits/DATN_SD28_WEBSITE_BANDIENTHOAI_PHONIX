package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.CauHinhGiaoCaDTO;
import com.example.datn_sd28_2025.entity.CauHinhGiaoCa;
import com.example.datn_sd28_2025.repository.CauHinhGiaoCaRepository;
import com.example.datn_sd28_2025.service.CauHinhGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CauHinhGiaoCaServiceImpl implements CauHinhGiaoCaService {

    @Autowired
    private CauHinhGiaoCaRepository cauHinhGiaoCaRepository;

    @Override
    public List<CauHinhGiaoCaDTO> getAll() {
        return cauHinhGiaoCaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CauHinhGiaoCaDTO> getById(Integer id) {
        return cauHinhGiaoCaRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public CauHinhGiaoCaDTO save(CauHinhGiaoCaDTO cauHinhGiaoCaDTO) {
        // Check if tenCauHinh already exists
        if (cauHinhGiaoCaRepository.existsByTenCauHinh(cauHinhGiaoCaDTO.getTenCauHinh())) {
            throw new RuntimeException("Tên cấu hình đã tồn tại trong hệ thống");
        }

        CauHinhGiaoCa cauHinhGiaoCa = convertToEntity(cauHinhGiaoCaDTO);
        CauHinhGiaoCa savedCauHinhGiaoCa = cauHinhGiaoCaRepository.save(cauHinhGiaoCa);
        return convertToDto(savedCauHinhGiaoCa);
    }

    @Override
    @Transactional
    public CauHinhGiaoCaDTO update(Integer id, CauHinhGiaoCaDTO cauHinhGiaoCaDTO) {
        CauHinhGiaoCa existingCauHinhGiaoCa = cauHinhGiaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cấu hình giao ca không tồn tại"));

        // Check if tenCauHinh already exists for another record
        if (cauHinhGiaoCaRepository.existsByTenCauHinhAndIdNot(cauHinhGiaoCaDTO.getTenCauHinh(), id)) {
            throw new RuntimeException("Tên cấu hình đã tồn tại cho cấu hình khác");
        }

        // Update fields
        existingCauHinhGiaoCa.setTenCauHinh(cauHinhGiaoCaDTO.getTenCauHinh());
        existingCauHinhGiaoCa.setGiaTri(cauHinhGiaoCaDTO.getGiaTri());
        existingCauHinhGiaoCa.setMoTa(cauHinhGiaoCaDTO.getMoTa());
        existingCauHinhGiaoCa.setLoaiCauHinh(cauHinhGiaoCaDTO.getLoaiCauHinh());
        existingCauHinhGiaoCa.setTrangThai(cauHinhGiaoCaDTO.getTrangThai());
        existingCauHinhGiaoCa.setNguoiCapNhat(cauHinhGiaoCaDTO.getNguoiCapNhat());

        CauHinhGiaoCa updatedCauHinhGiaoCa = cauHinhGiaoCaRepository.save(existingCauHinhGiaoCa);
        return convertToDto(updatedCauHinhGiaoCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!cauHinhGiaoCaRepository.existsById(id)) {
            throw new RuntimeException("Cấu hình giao ca không tồn tại");
        }
        cauHinhGiaoCaRepository.deleteById(id);
    }

    @Override
    public Optional<CauHinhGiaoCaDTO> getByTenCauHinh(String tenCauHinh) {
        return cauHinhGiaoCaRepository.findByTenCauHinh(tenCauHinh)
                .map(this::convertToDto);
    }

    @Override
    public List<CauHinhGiaoCaDTO> getByTrangThai(Integer trangThai) {
        return cauHinhGiaoCaRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CauHinhGiaoCaDTO> getByLoaiCauHinh(String loaiCauHinh) {
        return cauHinhGiaoCaRepository.findByLoaiCauHinh(loaiCauHinh).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CauHinhGiaoCaDTO> getActiveConfigurations() {
        return cauHinhGiaoCaRepository.findActiveConfigurations().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CauHinhGiaoCaDTO> searchByTenCauHinh(String tenCauHinh) {
        return cauHinhGiaoCaRepository.findByTenCauHinhContainingAndTrangThai(tenCauHinh, 1).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CauHinhGiaoCaDTO> getActiveByLoaiCauHinh(String loaiCauHinh) {
        return cauHinhGiaoCaRepository.findActiveByLoaiCauHinh(loaiCauHinh).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByTenCauHinh(String tenCauHinh) {
        return cauHinhGiaoCaRepository.existsByTenCauHinh(tenCauHinh);
    }

    @Override
    public boolean existsByTenCauHinhAndIdNot(String tenCauHinh, Integer id) {
        return cauHinhGiaoCaRepository.existsByTenCauHinhAndIdNot(tenCauHinh, id);
    }

    @Override
    public String getStringValue(String tenCauHinh, String defaultValue) {
        return getByTenCauHinh(tenCauHinh)
                .map(CauHinhGiaoCaDTO::getGiaTri)
                .orElse(defaultValue);
    }

    @Override
    public Integer getIntValue(String tenCauHinh, Integer defaultValue) {
        try {
            String value = getStringValue(tenCauHinh, null);
            return value != null ? Integer.parseInt(value) : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @Override
    public Boolean getBooleanValue(String tenCauHinh, Boolean defaultValue) {
        try {
            String value = getStringValue(tenCauHinh, null);
            return value != null ? Boolean.parseBoolean(value) : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    @Override
    @Transactional
    public void setValue(String tenCauHinh, String value) {
        Optional<CauHinhGiaoCaDTO> existingConfig = getByTenCauHinh(tenCauHinh);
        if (existingConfig.isPresent()) {
            CauHinhGiaoCaDTO config = existingConfig.get();
            config.setGiaTri(value);
            update(config.getId(), config);
        } else {
            // Create new configuration
            CauHinhGiaoCaDTO newConfig = CauHinhGiaoCaDTO.builder()
                    .tenCauHinh(tenCauHinh)
                    .giaTri(value)
                    .loaiCauHinh("TEXT")
                    .trangThai(1)
                    .nguoiTao("SYSTEM")
                    .build();
            save(newConfig);
        }
    }

    private CauHinhGiaoCaDTO convertToDto(CauHinhGiaoCa cauHinhGiaoCa) {
        return CauHinhGiaoCaDTO.builder()
                .id(cauHinhGiaoCa.getId())
                .tenCauHinh(cauHinhGiaoCa.getTenCauHinh())
                .giaTri(cauHinhGiaoCa.getGiaTri())
                .moTa(cauHinhGiaoCa.getMoTa())
                .loaiCauHinh(cauHinhGiaoCa.getLoaiCauHinh())
                .trangThai(cauHinhGiaoCa.getTrangThai())
                .trangThaiText(getTrangThaiText(cauHinhGiaoCa.getTrangThai()))
                .ngayTao(cauHinhGiaoCa.getNgayTao())
                .ngayCapNhat(cauHinhGiaoCa.getNgayCapNhat())
                .nguoiTao(cauHinhGiaoCa.getNguoiTao())
                .nguoiCapNhat(cauHinhGiaoCa.getNguoiCapNhat())
                .build();
    }

    private CauHinhGiaoCa convertToEntity(CauHinhGiaoCaDTO cauHinhGiaoCaDTO) {
        return CauHinhGiaoCa.builder()
                .id(cauHinhGiaoCaDTO.getId())
                .tenCauHinh(cauHinhGiaoCaDTO.getTenCauHinh())
                .giaTri(cauHinhGiaoCaDTO.getGiaTri())
                .moTa(cauHinhGiaoCaDTO.getMoTa())
                .loaiCauHinh(cauHinhGiaoCaDTO.getLoaiCauHinh())
                .trangThai(cauHinhGiaoCaDTO.getTrangThai())
                .nguoiTao(cauHinhGiaoCaDTO.getNguoiTao())
                .nguoiCapNhat(cauHinhGiaoCaDTO.getNguoiCapNhat())
                .build();
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 1 -> "Hoạt động";
            case 0 -> "Tạm dừng";
            default -> "Không xác định";
        };
    }
}




























