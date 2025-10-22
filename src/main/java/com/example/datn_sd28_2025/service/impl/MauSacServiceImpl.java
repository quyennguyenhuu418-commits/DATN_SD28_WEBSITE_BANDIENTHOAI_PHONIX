package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.MauSacDTO;
import com.example.datn_sd28_2025.entity.MauSac;
import com.example.datn_sd28_2025.repository.MauSacRepository;
import com.example.datn_sd28_2025.service.MauSacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MauSacServiceImpl implements MauSacService {

    @Autowired
    private MauSacRepository mauSacRepository;

    @Override
    public List<MauSacDTO> getAll() {
        return mauSacRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public List<MauSacDTO> getActive() {
        return mauSacRepository.findAllActive().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<MauSacDTO> getById(Integer id) {
        return mauSacRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public MauSacDTO save(MauSacDTO mauSacDTO) {
        MauSac mauSac = convertToEntity(mauSacDTO);
        mauSac.setNgayTao(LocalDateTime.now());
        mauSac.setTrangThai(1);
        return convertToDto(mauSacRepository.save(mauSac));
    }

    @Override
    public MauSacDTO update(Integer id, MauSacDTO mauSacDTO) {
        return mauSacRepository.findById(id).map(existingMauSac -> {
            existingMauSac.setMaMau(mauSacDTO.getMaMau());
            existingMauSac.setTenMau(mauSacDTO.getTenMau());
            existingMauSac.setMaHex(mauSacDTO.getMaHex());
            existingMauSac.setMoTa(mauSacDTO.getMoTa());
            existingMauSac.setTrangThai(mauSacDTO.getTrangThai());
            existingMauSac.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(mauSacRepository.save(existingMauSac));
        }).orElseThrow(() -> new RuntimeException("MauSac not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        // Kiểm tra xem mauSac có đang được sử dụng trong chi tiết sản phẩm không
        MauSac mauSac = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MauSac not found with id " + id));
        
        if (mauSac.getChiTietSanPhams() != null && !mauSac.getChiTietSanPhams().isEmpty()) {
            throw new RuntimeException("Không thể xóa màu sắc này vì đang được sử dụng trong chi tiết sản phẩm");
        }
        
        mauSacRepository.deleteById(id);
    }

    @Override
    public void updateStatus(Integer id, Integer trangThai) {
        MauSac mauSac = mauSacRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc với ID: " + id));
        mauSac.setTrangThai(trangThai);
        mauSac.setNgayCapNhat(LocalDateTime.now());
        mauSacRepository.save(mauSac);
    }

    private MauSacDTO convertToDto(MauSac mauSac) {
        return MauSacDTO.builder()
                .id(mauSac.getId())
                .maMau(mauSac.getMaMau())
                .tenMau(mauSac.getTenMau())
                .maHex(mauSac.getMaHex())
                .moTa(mauSac.getMoTa())
                .ngayTao(mauSac.getNgayTao())
                .ngayCapNhat(mauSac.getNgayCapNhat())
                .trangThai(mauSac.getTrangThai())
                .build();
    }

    private MauSac convertToEntity(MauSacDTO mauSacDTO) {
        return MauSac.builder()
                .id(mauSacDTO.getId())
                .maMau(mauSacDTO.getMaMau())
                .tenMau(mauSacDTO.getTenMau())
                .maHex(mauSacDTO.getMaHex())
                .moTa(mauSacDTO.getMoTa())
                .ngayTao(mauSacDTO.getNgayTao())
                .ngayCapNhat(mauSacDTO.getNgayCapNhat())
                .trangThai(mauSacDTO.getTrangThai())
                .build();
    }
}
