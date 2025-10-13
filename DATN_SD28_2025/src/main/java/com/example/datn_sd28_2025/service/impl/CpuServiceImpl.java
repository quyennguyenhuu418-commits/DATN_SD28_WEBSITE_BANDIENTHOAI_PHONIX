package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.CpuDTO;
import com.example.datn_sd28_2025.entity.Cpu;
import com.example.datn_sd28_2025.repository.CpuRepository;
import com.example.datn_sd28_2025.service.CpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CpuServiceImpl implements CpuService {

    @Autowired
    private CpuRepository cpuRepository;

    @Override
    public List<CpuDTO> getAll() {
        return cpuRepository.findAll().stream()
                .map(this::convertToDto).toList();
    }

    @Override
    public Page<CpuDTO> getAll(Pageable pageable) {
        return cpuRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Optional<CpuDTO> getById(Integer id) {
        return cpuRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public CpuDTO save(CpuDTO cpuDTO) {
        Cpu cpu = convertToEntity(cpuDTO);
        cpu.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (cpu.getTrangThai() == null) {
            cpu.setTrangThai(1);
        }
        return convertToDto(cpuRepository.save(cpu));
    }

    @Override
    public CpuDTO update(Integer id, CpuDTO cpuDTO) {
        return cpuRepository.findById(id)
                .map(existingCpu -> {
                    existingCpu.setMaCpu(cpuDTO.getMaCpu());
                    existingCpu.setTenCpu(cpuDTO.getTenCpu());
                    existingCpu.setMoTa(cpuDTO.getMoTa());
                    existingCpu.setTrangThai(cpuDTO.getTrangThai());
                    existingCpu.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(cpuRepository.save(existingCpu));
                }).orElseThrow(() -> new RuntimeException("Cpu not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem cpu có đang được sử dụng trong sản phẩm không
        Cpu cpu = cpuRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cpu not found with id " + id));
        
        if (cpu.getSanPhams() != null && !cpu.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa CPU này vì đang được sử dụng trong sản phẩm");
        }
        
        cpuRepository.deleteById(id);
    }

    private CpuDTO convertToDto(Cpu cpu) {
        return CpuDTO.builder()
                .id(cpu.getId())
                .maCpu(cpu.getMaCpu())
                .tenCpu(cpu.getTenCpu())
                .moTa(cpu.getMoTa())
                .ngayTao(cpu.getNgayTao())
                .ngayCapNhat(cpu.getNgayCapNhat())
                .trangThai(cpu.getTrangThai())
                .build();
    }

    private Cpu convertToEntity(CpuDTO cpuDTO) {
        return Cpu.builder()
                .id(cpuDTO.getId())
                .maCpu(cpuDTO.getMaCpu())
                .tenCpu(cpuDTO.getTenCpu())
                .moTa(cpuDTO.getMoTa())
                .ngayTao(cpuDTO.getNgayTao())
                .ngayCapNhat(cpuDTO.getNgayCapNhat())
                .trangThai(cpuDTO.getTrangThai())
                .build();
    }
}
