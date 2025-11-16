package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.ManHinhDTO;
import com.example.datn_sd28_2025.entity.ManHinh;
import com.example.datn_sd28_2025.repository.ManHinhRepository;
import com.example.datn_sd28_2025.service.ManHinhService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ManHinhServiceImpl implements ManHinhService {

    @Autowired
    private ManHinhRepository manHinhRepository;

    @Override
    public List<ManHinhDTO> getAll() {
        return manHinhRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<ManHinhDTO> getActive() {
        return manHinhRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<ManHinhDTO> getById(Integer id) {
        return manHinhRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public ManHinhDTO save(ManHinhDTO manHinhDTO) {
        ManHinh manHinh = convertToEntity(manHinhDTO);
        manHinh.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (manHinh.getTrangThai() == null) {
            manHinh.setTrangThai(1);
        }
        return convertToDto(manHinhRepository.save(manHinh));
    }

    @Override
    public ManHinhDTO update(Integer id, ManHinhDTO manHinhDTO) {
        return manHinhRepository.findById(id).map(existingManHinh -> {
            existingManHinh.setMaManHinh(manHinhDTO.getMaManHinh());
            existingManHinh.setKichThuoc(manHinhDTO.getKichThuoc());
            existingManHinh.setCongNghe(manHinhDTO.getCongNghe());
            existingManHinh.setDoPhanGiai(manHinhDTO.getDoPhanGiai());
            existingManHinh.setTanSoQuet(manHinhDTO.getTanSoQuet());
            existingManHinh.setKieuManHinh(manHinhDTO.getKieuManHinh());
            existingManHinh.setMoTa(manHinhDTO.getMoTa());
            existingManHinh.setTrangThai(manHinhDTO.getTrangThai());
            existingManHinh.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(manHinhRepository.save(existingManHinh));
        }).orElseThrow(() -> new RuntimeException("ManHinh not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem manHinh có đang được sử dụng trong sản phẩm không
        ManHinh manHinh = manHinhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ManHinh not found with id " + id));
        
        if (manHinh.getSanPhams() != null && !manHinh.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa màn hình này vì đang được sử dụng trong sản phẩm");
        }
        
        manHinhRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        ManHinh manHinh = manHinhRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màn hình với ID: " + id));
        manHinh.setTrangThai(trangThai);
        manHinh.setNgayCapNhat(LocalDateTime.now());
        manHinhRepository.save(manHinh);
    }

    private ManHinhDTO convertToDto(ManHinh manHinh) {
        return ManHinhDTO.builder()
                .id(manHinh.getId())
                .maManHinh(manHinh.getMaManHinh())
                .kichThuoc(manHinh.getKichThuoc())
                .congNghe(manHinh.getCongNghe())
                .doPhanGiai(manHinh.getDoPhanGiai())
                .tanSoQuet(manHinh.getTanSoQuet())
                .kieuManHinh(manHinh.getKieuManHinh())
                .moTa(manHinh.getMoTa())
                .ngayTao(manHinh.getNgayTao())
                .ngayCapNhat(manHinh.getNgayCapNhat())
                .trangThai(manHinh.getTrangThai())
                .build();
    }

    private ManHinh convertToEntity(ManHinhDTO manHinhDTO) {
        return ManHinh.builder()
                .id(manHinhDTO.getId())
                .maManHinh(manHinhDTO.getMaManHinh())
                .kichThuoc(manHinhDTO.getKichThuoc())
                .congNghe(manHinhDTO.getCongNghe())
                .doPhanGiai(manHinhDTO.getDoPhanGiai())
                .tanSoQuet(manHinhDTO.getTanSoQuet())
                .kieuManHinh(manHinhDTO.getKieuManHinh())
                .moTa(manHinhDTO.getMoTa())
                .ngayTao(manHinhDTO.getNgayTao())
                .ngayCapNhat(manHinhDTO.getNgayCapNhat())
                .trangThai(manHinhDTO.getTrangThai())
                .build();
    }
}
