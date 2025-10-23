package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.RamDTO;
import com.example.datn_sd28_2025.entity.Ram;
import com.example.datn_sd28_2025.repository.RamRepository;
import com.example.datn_sd28_2025.service.RamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RamServiceImpl implements RamService {

    @Autowired
    private RamRepository ramRepository;

    @Override
    public List<RamDTO> getAll() {
        return ramRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<RamDTO> getActive() {
        return ramRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Page<RamDTO> getAll(Pageable pageable) {
        return ramRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public Optional<RamDTO> getById(Integer id) {
        return ramRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public RamDTO save(RamDTO ramDTO) {
        Ram ram = convertToEntity(ramDTO);
        ram.setNgayTao(LocalDateTime.now());
        ram.setTrangThai(1);
        return convertToDto(ramRepository.save(ram));
    }

    @Override
    public RamDTO update(Integer id, RamDTO ramDTO) {
        return ramRepository.findById(id).map(existingRam -> {
            existingRam.setMaRam(ramDTO.getMaRam());
            existingRam.setTenRam(ramDTO.getTenRam());
            existingRam.setMoTa(ramDTO.getMoTa());
            existingRam.setTrangThai(ramDTO.getTrangThai());
            existingRam.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(ramRepository.save(existingRam));
        }).orElseThrow(() -> new RuntimeException("Ram not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem ram có đang được sử dụng trong chi tiết sản phẩm không
        Ram ram = ramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ram not found with id " + id));
        
        if (ram.getChiTietSanPhams() != null && !ram.getChiTietSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa RAM này vì đang được sử dụng trong chi tiết sản phẩm");
        }
        
        ramRepository.deleteById(id);
    }

<<<<<<< HEAD
    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Ram ram = ramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ram với ID: " + id));
        ram.setTrangThai(trangThai);
        ram.setNgayCapNhat(LocalDateTime.now());
        ramRepository.save(ram);
    }

=======
>>>>>>> origin/Huan
    private RamDTO convertToDto(Ram ram) {
        return RamDTO.builder()
                .id(ram.getId())
                .maRam(ram.getMaRam())
                .tenRam(ram.getTenRam())
                .moTa(ram.getMoTa())
                .ngayTao(ram.getNgayTao())
                .ngayCapNhat(ram.getNgayCapNhat())
                .trangThai(ram.getTrangThai())
                .build();
    }

    private Ram convertToEntity(RamDTO ramDTO) {
        return Ram.builder()
                .id(ramDTO.getId())
                .maRam(ramDTO.getMaRam())
                .tenRam(ramDTO.getTenRam())
                .moTa(ramDTO.getMoTa())
                .ngayTao(ramDTO.getNgayTao())
                .ngayCapNhat(ramDTO.getNgayCapNhat())
                .trangThai(ramDTO.getTrangThai())
                .build();
    }
<<<<<<< HEAD
=======

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Ram ram = ramRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ram với ID: " + id));
        ram.setTrangThai(trangThai);
        ram.setNgayCapNhat(LocalDateTime.now());
        ramRepository.save(ram);
    }
>>>>>>> origin/Huan
}
