package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.RomDTO;
import com.example.datn_sd28_2025.entity.Rom;
import com.example.datn_sd28_2025.repository.RomRepository;
import com.example.datn_sd28_2025.service.RomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class RomServiceImpl implements RomService {

    @Autowired
    private RomRepository romRepository;

    @Override
    public List<RomDTO> getAll() {
        return romRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Page<RomDTO> getAll(Pageable pageable) {
        return romRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public Optional<RomDTO> getById(Integer id) {
        return romRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public RomDTO save(RomDTO romDTO) {
        Rom rom = convertToEntity(romDTO);
        rom.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (rom.getTrangThai() == null) {
            rom.setTrangThai(1);
        }
        return convertToDto(romRepository.save(rom));
    }

    @Override
    public RomDTO update(Integer id, RomDTO romDTO) {
        return romRepository.findById(id).map(existingRom -> {
            existingRom.setMaRom(romDTO.getMaRom());
            existingRom.setDungLuong(romDTO.getDungLuong());
            existingRom.setMoTa(romDTO.getMoTa());
            existingRom.setTrangThai(romDTO.getTrangThai());
            existingRom.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(romRepository.save(existingRom));
        }).orElseThrow(() -> new RuntimeException("Rom not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem rom có đang được sử dụng trong chi tiết sản phẩm không
        Rom rom = romRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Rom not found with id " + id));
        
        if (rom.getChiTietSanPhams() != null && !rom.getChiTietSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa ROM này vì đang được sử dụng trong chi tiết sản phẩm");
        }
        
        romRepository.deleteById(id);
    }

    private RomDTO convertToDto(Rom rom) {
        return RomDTO.builder()
                .id(rom.getId())
                .maRom(rom.getMaRom())
                .dungLuong(rom.getDungLuong())
                .moTa(rom.getMoTa())
                .ngayTao(rom.getNgayTao())
                .ngayCapNhat(rom.getNgayCapNhat())
                .trangThai(rom.getTrangThai())
                .build();
    }

    private Rom convertToEntity(RomDTO romDTO) {
        return Rom.builder()
                .id(romDTO.getId())
                .maRom(romDTO.getMaRom())
                .dungLuong(romDTO.getDungLuong())
                .moTa(romDTO.getMoTa())
                .ngayTao(romDTO.getNgayTao())
                .ngayCapNhat(romDTO.getNgayCapNhat())
                .trangThai(romDTO.getTrangThai())
                .build();
    }
}
