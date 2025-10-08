package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.ChipDTO;
import com.example.datn_sd28_2025.entity.Chip;
import com.example.datn_sd28_2025.repository.ChipRepository;
import com.example.datn_sd28_2025.service.ChipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ChipServiceImpl implements ChipService {

    @Autowired
    private ChipRepository chipRepository;

    @Override
    public List<ChipDTO> getAll() {
        return chipRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<ChipDTO> getActive() {
        return chipRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<ChipDTO> getById(Integer id) {
        return chipRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public ChipDTO save(ChipDTO chipDTO) {
        Chip chip = convertToEntity(chipDTO);
        chip.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (chip.getTrangThai() == null) {
            chip.setTrangThai(1);
        }
        return convertToDto(chipRepository.save(chip));
    }

    @Override
    public ChipDTO update(Integer id, ChipDTO chipDTO) {
        return chipRepository.findById(id).map(existingChip -> {
            existingChip.setMaChip(chipDTO.getMaChip());
            existingChip.setTenChip(chipDTO.getTenChip());
            existingChip.setMoTa(chipDTO.getMoTa());
            existingChip.setTrangThai(chipDTO.getTrangThai());
            existingChip.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(chipRepository.save(existingChip));
        }).orElseThrow(() -> new RuntimeException("Chip not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem chip có đang được sử dụng trong sản phẩm không
        Chip chip = chipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chip not found with id " + id));
        
        if (chip.getSanPhams() != null && !chip.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa chip này vì đang được sử dụng trong sản phẩm");
        }
        
        chipRepository.deleteById(id);
    }

    private ChipDTO convertToDto(Chip chip) {
        return ChipDTO.builder()
                .id(chip.getId())
                .maChip(chip.getMaChip())
                .tenChip(chip.getTenChip())
                .moTa(chip.getMoTa())
                .ngayTao(chip.getNgayTao())
                .ngayCapNhat(chip.getNgayCapNhat())
                .trangThai(chip.getTrangThai())
                .build();
    }

    private Chip convertToEntity(ChipDTO chipDTO) {
        return Chip.builder()
                .id(chipDTO.getId())
                .maChip(chipDTO.getMaChip())
                .tenChip(chipDTO.getTenChip())
                .moTa(chipDTO.getMoTa())
                .ngayTao(chipDTO.getNgayTao())
                .ngayCapNhat(chipDTO.getNgayCapNhat())
                .trangThai(chipDTO.getTrangThai())
                .build();
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Chip chip = chipRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chip với ID: " + id));
        chip.setTrangThai(trangThai);
        chip.setNgayCapNhat(LocalDateTime.now());
        chipRepository.save(chip);
    }
}
