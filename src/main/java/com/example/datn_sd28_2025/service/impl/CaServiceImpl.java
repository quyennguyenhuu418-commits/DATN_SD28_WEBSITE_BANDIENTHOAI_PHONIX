package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.CaDTO;
import com.example.datn_sd28_2025.entity.Ca;
import com.example.datn_sd28_2025.repository.CaRepository;
import com.example.datn_sd28_2025.service.CaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CaServiceImpl implements CaService {

    @Autowired
    private CaRepository caRepository;

    @Override
    public List<CaDTO> getAll() {
        return caRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<CaDTO> getById(Integer id) {
        return caRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public CaDTO save(CaDTO caDTO) {
        // Check if maCa already exists
        if (caRepository.existsByMaCa(caDTO.getMaCa())) {
            throw new RuntimeException("Mã ca đã tồn tại trong hệ thống");
        }

        Ca ca = convertToEntity(caDTO);
        Ca savedCa = caRepository.save(ca);
        return convertToDto(savedCa);
    }

    @Override
    @Transactional
    public CaDTO update(Integer id, CaDTO caDTO) {
        Ca existingCa = caRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ca không tồn tại"));

        // Check if maCa already exists for another ca
        if (caRepository.existsByMaCaAndIdNot(caDTO.getMaCa(), id)) {
            throw new RuntimeException("Mã ca đã tồn tại cho ca khác");
        }

        // Update fields
        existingCa.setMaCa(caDTO.getMaCa());
        existingCa.setTenCa(caDTO.getTenCa());
        existingCa.setMoTa(caDTO.getMoTa());
        existingCa.setGioBatDau(caDTO.getGioBatDau());
        existingCa.setGioKetThuc(caDTO.getGioKetThuc());
        existingCa.setTrangThai(caDTO.getTrangThai());
        existingCa.setNguoiCapNhat(caDTO.getNguoiCapNhat());

        Ca updatedCa = caRepository.save(existingCa);
        return convertToDto(updatedCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!caRepository.existsById(id)) {
            throw new RuntimeException("Ca không tồn tại");
        }
        caRepository.deleteById(id);
    }

    @Override
    public List<CaDTO> getActiveCa() {
        return caRepository.findActiveCaOrderByGioBatDau().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaDTO> getCurrentActiveCa() {
        LocalTime currentTime = LocalTime.now();
        return caRepository.findCurrentActiveCa(currentTime).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CaDTO> searchByTenCa(String tenCa) {
        return caRepository.findByTenCaContainingAndTrangThai(tenCa, 1).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByMaCa(String maCa) {
        return caRepository.existsByMaCa(maCa);
    }

    @Override
    public boolean existsByMaCaAndIdNot(String maCa, Integer id) {
        return caRepository.existsByMaCaAndIdNot(maCa, id);
    }

    private CaDTO convertToDto(Ca ca) {
        return CaDTO.builder()
                .id(ca.getId())
                .maCa(ca.getMaCa())
                .tenCa(ca.getTenCa())
                .moTa(ca.getMoTa())
                .gioBatDau(ca.getGioBatDau())
                .gioKetThuc(ca.getGioKetThuc())
                .trangThai(ca.getTrangThai())
                .trangThaiText(getTrangThaiText(ca.getTrangThai()))
                .ngayTao(ca.getNgayTao())
                .ngayCapNhat(ca.getNgayCapNhat())
                .nguoiTao(ca.getNguoiTao())
                .nguoiCapNhat(ca.getNguoiCapNhat())
                .build();
    }

    private Ca convertToEntity(CaDTO caDTO) {
        return Ca.builder()
                .id(caDTO.getId())
                .maCa(caDTO.getMaCa())
                .tenCa(caDTO.getTenCa())
                .moTa(caDTO.getMoTa())
                .gioBatDau(caDTO.getGioBatDau())
                .gioKetThuc(caDTO.getGioKetThuc())
                .trangThai(caDTO.getTrangThai())
                .nguoiTao(caDTO.getNguoiTao())
                .nguoiCapNhat(caDTO.getNguoiCapNhat())
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

