package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.CameraSauDTO;
import com.example.datn_sd28_2025.entity.CameraSau;
import com.example.datn_sd28_2025.repository.CameraSauRepository;
import com.example.datn_sd28_2025.service.CameraSauService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CameraSauServiceImpl implements CameraSauService {

    @Autowired
    private CameraSauRepository cameraSauRepository;

    @Override
    public List<CameraSauDTO> getAll() {
        return cameraSauRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<CameraSauDTO> getActive() {
        return cameraSauRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<CameraSauDTO> getById(Integer id) {
        return cameraSauRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public CameraSauDTO save(CameraSauDTO cameraSauDTO) {
        CameraSau cameraSau = convertToEntity(cameraSauDTO);
        cameraSau.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (cameraSau.getTrangThai() == null) {
            cameraSau.setTrangThai(1);
        }
        return convertToDto(cameraSauRepository.save(cameraSau));
    }

    @Override
    public CameraSauDTO update(Integer id, CameraSauDTO cameraSauDTO) {
        return cameraSauRepository.findById(id).map(existingCamera -> {
            existingCamera.setMaCamera(cameraSauDTO.getMaCamera());
            existingCamera.setThongSo(cameraSauDTO.getThongSo());
            existingCamera.setMoTa(cameraSauDTO.getMoTa());
            existingCamera.setTrangThai(cameraSauDTO.getTrangThai());
            existingCamera.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(cameraSauRepository.save(existingCamera));
        }).orElseThrow(() -> new RuntimeException("CameraSau not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem cameraSau có đang được sử dụng trong sản phẩm không
        CameraSau cameraSau = cameraSauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CameraSau not found with id " + id));
        
        if (cameraSau.getSanPhams() != null && !cameraSau.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa camera sau này vì đang được sử dụng trong sản phẩm");
        }
        
        cameraSauRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        CameraSau cameraSau = cameraSauRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy camera sau với ID: " + id));
        cameraSau.setTrangThai(trangThai);
        cameraSau.setNgayCapNhat(LocalDateTime.now());
        cameraSauRepository.save(cameraSau);
    }

    private CameraSauDTO convertToDto(CameraSau cameraSau) {
        return CameraSauDTO.builder()
                .id(cameraSau.getId())
                .maCamera(cameraSau.getMaCamera())
                .thongSo(cameraSau.getThongSo())
                .moTa(cameraSau.getMoTa())
                .ngayTao(cameraSau.getNgayTao())
                .ngayCapNhat(cameraSau.getNgayCapNhat())
                .trangThai(cameraSau.getTrangThai())
                .build();
    }

    private CameraSau convertToEntity(CameraSauDTO cameraSauDTO) {
        return CameraSau.builder()
                .id(cameraSauDTO.getId())
                .maCamera(cameraSauDTO.getMaCamera())
                .thongSo(cameraSauDTO.getThongSo())
                .moTa(cameraSauDTO.getMoTa())
                .ngayTao(cameraSauDTO.getNgayTao())
                .ngayCapNhat(cameraSauDTO.getNgayCapNhat())
                .trangThai(cameraSauDTO.getTrangThai())
                .build();
    }
}
