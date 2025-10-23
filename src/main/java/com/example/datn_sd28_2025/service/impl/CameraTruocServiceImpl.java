package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.CameraTruocDTO;
import com.example.datn_sd28_2025.entity.CameraTruoc;
import com.example.datn_sd28_2025.repository.CameraTruocRepository;
import com.example.datn_sd28_2025.service.CameraTruocService;
import org.springframework.beans.factory.annotation.Autowired;
<<<<<<< HEAD
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
=======

>>>>>>> origin/Huan
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CameraTruocServiceImpl implements CameraTruocService {

    @Autowired
    private CameraTruocRepository cameraTruocRepository;

    @Override
    public List<CameraTruocDTO> getAll() {
        return cameraTruocRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<CameraTruocDTO> getActive() {
        return cameraTruocRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<CameraTruocDTO> getById(Integer id) {
        return cameraTruocRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public CameraTruocDTO save(CameraTruocDTO cameraTruocDTO) {
        CameraTruoc cameraTruoc = convertToEntity(cameraTruocDTO);
        cameraTruoc.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (cameraTruoc.getTrangThai() == null) {
            cameraTruoc.setTrangThai(1);
        }
        return convertToDto(cameraTruocRepository.save(cameraTruoc));
    }

    @Override
    public CameraTruocDTO update(Integer id, CameraTruocDTO cameraTruocDTO) {
        return cameraTruocRepository.findById(id).map(existingCamera -> {
            existingCamera.setMaCamera(cameraTruocDTO.getMaCamera());
            existingCamera.setThongSo(cameraTruocDTO.getThongSo());
            existingCamera.setMoTa(cameraTruocDTO.getMoTa());
            existingCamera.setTrangThai(cameraTruocDTO.getTrangThai());
            existingCamera.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(cameraTruocRepository.save(existingCamera));
        }).orElseThrow(() -> new RuntimeException("CameraTruoc not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem cameraTruoc có đang được sử dụng trong sản phẩm không
        CameraTruoc cameraTruoc = cameraTruocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("CameraTruoc not found with id " + id));
        
        if (cameraTruoc.getSanPhams() != null && !cameraTruoc.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa camera trước này vì đang được sử dụng trong sản phẩm");
        }
        
        cameraTruocRepository.deleteById(id);
    }

<<<<<<< HEAD
    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        CameraTruoc cameraTruoc = cameraTruocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy camera trước với ID: " + id));
        cameraTruoc.setTrangThai(trangThai);
        cameraTruoc.setNgayCapNhat(LocalDateTime.now());
        cameraTruocRepository.save(cameraTruoc);
    }

=======
>>>>>>> origin/Huan
    private CameraTruocDTO convertToDto(CameraTruoc cameraTruoc) {
        return CameraTruocDTO.builder()
                .id(cameraTruoc.getId())
                .maCamera(cameraTruoc.getMaCamera())
                .thongSo(cameraTruoc.getThongSo())
                .moTa(cameraTruoc.getMoTa())
                .ngayTao(cameraTruoc.getNgayTao())
                .ngayCapNhat(cameraTruoc.getNgayCapNhat())
                .trangThai(cameraTruoc.getTrangThai())
                .build();
    }

    private CameraTruoc convertToEntity(CameraTruocDTO cameraTruocDTO) {
        return CameraTruoc.builder()
                .id(cameraTruocDTO.getId())
                .maCamera(cameraTruocDTO.getMaCamera())
                .thongSo(cameraTruocDTO.getThongSo())
                .moTa(cameraTruocDTO.getMoTa())
                .ngayTao(cameraTruocDTO.getNgayTao())
                .ngayCapNhat(cameraTruocDTO.getNgayCapNhat())
                .trangThai(cameraTruocDTO.getTrangThai())
                .build();
    }
<<<<<<< HEAD
=======

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        CameraTruoc cameraTruoc = cameraTruocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy camera trước với ID: " + id));
        cameraTruoc.setTrangThai(trangThai);
        cameraTruoc.setNgayCapNhat(LocalDateTime.now());
        cameraTruocRepository.save(cameraTruoc);
    }
>>>>>>> origin/Huan
}
