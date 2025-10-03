package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.GpuDTO;
import com.example.datn_sd28_2025.entity.Gpu;
import com.example.datn_sd28_2025.repository.GpuRepository;
import com.example.datn_sd28_2025.service.GpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GpuServiceImpl implements GpuService {

    @Autowired
    private GpuRepository gpuRepository;

    @Override
    public List<GpuDTO> getAll() {
        return gpuRepository.findAll().stream()
                .map(this::convertToDto).toList();
    }

    @Override
    public Page<GpuDTO> getAll(Pageable pageable) {
        return gpuRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Optional<GpuDTO> getById(Integer id) {
        return gpuRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public GpuDTO save(GpuDTO gpuDTO) {
        Gpu gpu = convertToEntity(gpuDTO);
        gpu.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (gpu.getTrangThai() == null) {
            gpu.setTrangThai(1);
        }
        return convertToDto(gpuRepository.save(gpu));
    }

    @Override
    public GpuDTO update(Integer id, GpuDTO gpuDTO) {
        return gpuRepository.findById(id)
                .map(existingGpu -> {
                    existingGpu.setMaGpu(gpuDTO.getMaGpu());
                    existingGpu.setTenGpu(gpuDTO.getTenGpu());
                    existingGpu.setMoTa(gpuDTO.getMoTa());
                    existingGpu.setTrangThai(gpuDTO.getTrangThai());
                    existingGpu.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(gpuRepository.save(existingGpu));
                }).orElseThrow(() -> new RuntimeException("Gpu not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem gpu có đang được sử dụng trong sản phẩm không
        Gpu gpu = gpuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gpu not found with id " + id));
        
        if (gpu.getSanPhams() != null && !gpu.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa GPU này vì đang được sử dụng trong sản phẩm");
        }
        
        gpuRepository.deleteById(id);
    }

    private GpuDTO convertToDto(Gpu gpu) {
        return GpuDTO.builder()
                .id(gpu.getId())
                .maGpu(gpu.getMaGpu())
                .tenGpu(gpu.getTenGpu())
                .moTa(gpu.getMoTa())
                .ngayTao(gpu.getNgayTao())
                .ngayCapNhat(gpu.getNgayCapNhat())
                .trangThai(gpu.getTrangThai())
                .build();
    }

    private Gpu convertToEntity(GpuDTO gpuDTO) {
        return Gpu.builder()
                .id(gpuDTO.getId())
                .maGpu(gpuDTO.getMaGpu())
                .tenGpu(gpuDTO.getTenGpu())
                .moTa(gpuDTO.getMoTa())
                .ngayTao(gpuDTO.getNgayTao())
                .ngayCapNhat(gpuDTO.getNgayCapNhat())
                .trangThai(gpuDTO.getTrangThai())
                .build();
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Gpu gpu = gpuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy gpu với ID: " + id));
        gpu.setTrangThai(trangThai);
        gpu.setNgayCapNhat(LocalDateTime.now());
        gpuRepository.save(gpu);
    }
}
