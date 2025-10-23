package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.HangDTO;
import com.example.datn_sd28_2025.entity.Hang;
import com.example.datn_sd28_2025.repository.HangRepository;
import com.example.datn_sd28_2025.service.HangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HangServiceImpl implements HangService {

    @Autowired
    private HangRepository hangRepository;

    @Override
    public List<HangDTO> getAll() {
        return hangRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<HangDTO> getActive() {
        return hangRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<HangDTO> getById(Integer id) {
        return hangRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public HangDTO save(HangDTO hangDTO) {
        Hang hang = convertToEntity(hangDTO);
        hang.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (hang.getTrangThai() == null) {
            hang.setTrangThai(1);
        }
        return convertToDto(hangRepository.save(hang));
    }

    @Override
    public HangDTO update(Integer id, HangDTO hangDTO) {
        return hangRepository.findById(id).map(existingHang -> {
            existingHang.setTen(hangDTO.getTen());
            existingHang.setXuatXu(hangDTO.getXuatXu());
            existingHang.setMoTa(hangDTO.getMoTa());
            existingHang.setTrangThai(hangDTO.getTrangThai());
            existingHang.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(hangRepository.save(existingHang));
        }).orElseThrow(() -> new RuntimeException("Hang not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem hang có đang được sử dụng trong sản phẩm không
        Hang hang = hangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hang not found with id " + id));
        
        if (hang.getSanPhams() != null && !hang.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa hãng này vì đang được sử dụng trong sản phẩm");
        }
        
        hangRepository.deleteById(id);
    }

<<<<<<< HEAD
    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Hang hang = hangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng với ID: " + id));
        hang.setTrangThai(trangThai);
        hang.setNgayCapNhat(LocalDateTime.now());
        hangRepository.save(hang);
    }

=======
>>>>>>> origin/Huan
    private HangDTO convertToDto(Hang hang) {
        return HangDTO.builder()
                .id(hang.getId())
                .ten(hang.getTen())
                .xuatXu(hang.getXuatXu())
                .moTa(hang.getMoTa())
                .ngayTao(hang.getNgayTao())
                .ngayCapNhat(hang.getNgayCapNhat())
                .trangThai(hang.getTrangThai())
                .build();
    }

    private Hang convertToEntity(HangDTO hangDTO) {
        return Hang.builder()
                .id(hangDTO.getId())
                .ten(hangDTO.getTen())
                .xuatXu(hangDTO.getXuatXu())
                .moTa(hangDTO.getMoTa())
                .ngayTao(hangDTO.getNgayTao())
                .ngayCapNhat(hangDTO.getNgayCapNhat())
                .trangThai(hangDTO.getTrangThai())
                .build();
    }
<<<<<<< HEAD
=======

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Hang hang = hangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hãng với ID: " + id));
        hang.setTrangThai(trangThai);
        hang.setNgayCapNhat(LocalDateTime.now());
        hangRepository.save(hang);
    }
>>>>>>> origin/Huan
}
