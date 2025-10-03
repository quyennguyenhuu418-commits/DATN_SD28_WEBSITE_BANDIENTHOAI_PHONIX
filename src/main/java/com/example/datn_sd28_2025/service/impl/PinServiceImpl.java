package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.PinDTO;
import com.example.datn_sd28_2025.entity.Pin;
import com.example.datn_sd28_2025.repository.PinRepository;
import com.example.datn_sd28_2025.service.PinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PinServiceImpl implements PinService {

    @Autowired
    private PinRepository pinRepository;

    @Override
    public List<PinDTO> getAll() {
        return pinRepository.findAll().stream()
                .map(this::convertToDto).toList();
    }

    @Override
    public Page<PinDTO> getAll(Pageable pageable) {
        return pinRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Override
    public Optional<PinDTO> getById(Integer id) {
        return pinRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public PinDTO save(PinDTO pinDTO) {
        Pin pin = convertToEntity(pinDTO);
        pin.setNgayTao(LocalDateTime.now());
        // Sử dụng trangThai từ DTO, nếu null thì mặc định là 1
        if (pin.getTrangThai() == null) {
            pin.setTrangThai(1);
        }
        return convertToDto(pinRepository.save(pin));
    }

    @Override
    public PinDTO update(Integer id, PinDTO pinDTO) {
        return pinRepository.findById(id)
                .map(existingPin -> {
                    existingPin.setMaPin(pinDTO.getMaPin());
                    existingPin.setDungLuongPin(pinDTO.getDungLuongPin());
                    existingPin.setCongNgheSac(pinDTO.getCongNgheSac());
                    existingPin.setMoTa(pinDTO.getMoTa());
                    existingPin.setTrangThai(pinDTO.getTrangThai());
                    existingPin.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(pinRepository.save(existingPin));
                }).orElseThrow(() -> new RuntimeException("Pin not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem pin có đang được sử dụng trong sản phẩm không
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pin not found with id " + id));
        
        if (pin.getSanPhams() != null && !pin.getSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa pin này vì đang được sử dụng trong sản phẩm");
        }
        
        pinRepository.deleteById(id); // Hard delete
    }

    private PinDTO convertToDto(Pin pin) {
        return PinDTO.builder()
                .id(pin.getId())
                .maPin(pin.getMaPin())
                .dungLuongPin(pin.getDungLuongPin())
                .congNgheSac(pin.getCongNgheSac())
                .moTa(pin.getMoTa())
                .ngayTao(pin.getNgayTao())
                .ngayCapNhat(pin.getNgayCapNhat())
                .trangThai(pin.getTrangThai())
                .build();
    }

    private Pin convertToEntity(PinDTO pinDTO) {
        return Pin.builder()
                .id(pinDTO.getId())
                .maPin(pinDTO.getMaPin())
                .dungLuongPin(pinDTO.getDungLuongPin())
                .congNgheSac(pinDTO.getCongNgheSac())
                .moTa(pinDTO.getMoTa())
                .ngayTao(pinDTO.getNgayTao())
                .ngayCapNhat(pinDTO.getNgayCapNhat())
                .trangThai(pinDTO.getTrangThai())
                .build();
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        Pin pin = pinRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy pin với ID: " + id));
        pin.setTrangThai(trangThai);
        pin.setNgayCapNhat(LocalDateTime.now());
        pinRepository.save(pin);
    }
}
