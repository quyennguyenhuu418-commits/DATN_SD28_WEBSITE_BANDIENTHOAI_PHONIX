package com.example.datn_sd28_2025.service.impl;

import jakarta.transaction.Transactional;
import com.example.datn_sd28_2025.dto.ImeiDTO;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.service.ImeiService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ImeiServiceImpl implements ImeiService {

    private final ImeiRepository imeiRepository;
    private final ChiTietSanPhamRepository chiTietSanPhamRepository;

    public ImeiServiceImpl(ImeiRepository imeiRepository, ChiTietSanPhamRepository chiTietSanPhamRepository) {
        this.imeiRepository = imeiRepository;
        this.chiTietSanPhamRepository = chiTietSanPhamRepository;
    }

    @Override
    public Imei create(Imei imei) {
        // Note: ngay_tao column doesn't exist in the actual database
        // imei.setNgayTao(LocalDateTime.now());
        if (imei.getTrangThai() == null) imei.setTrangThai(1);
        return imeiRepository.save(imei);
    }

    @Override
    @Transactional
    public List<Imei> bulkCreate(Integer chiTietId, List<String> imeis) {
        ChiTietSanPham ct = chiTietSanPhamRepository.findById(chiTietId)
                .orElseThrow(() -> new IllegalArgumentException("ChiTietSanPham not found"));
        List<Imei> saved = new ArrayList<>();
        for (String s : imeis) {
            String code = s.trim();
            if (code.isEmpty()) continue;
            if (imeiRepository.findByImei(code).isPresent()) continue; // skip duplicates
            Imei i = new Imei();
            i.setImei(code);
            i.setChiTietSanPham(ct);
            i.setTrangThai(1);
            // Note: ngay_tao column doesn't exist in the actual database
            // i.setNgayTao(LocalDateTime.now());
            saved.add(imeiRepository.save(i));
        }
        return saved;
    }

    @Override
    public List<Imei> findByChiTiet(Integer chiTietId, Integer status) {
        return imeiRepository.findByChiTietAndStatus(chiTietId, status);
    }

    @Override
    public Imei updateStatus(Integer id, Integer status) {
        Imei i = imeiRepository.findById(id).orElseThrow();
        i.setTrangThai(status);
        return imeiRepository.save(i);
    }

    // DTO methods
    @Override
    public List<ImeiDTO> getAll() {
        return imeiRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public ImeiDTO getById(Integer id) {
        return imeiRepository.findById(id).map(this::convertToDto).orElse(null);
    }

    @Override
    public ImeiDTO save(ImeiDTO imeiDTO) {
        Imei imei = convertToEntity(imeiDTO);
        // Note: ngay_tao column doesn't exist in the actual database
        // imei.setNgayTao(LocalDateTime.now());
        imei.setTrangThai(1);
        return convertToDto(imeiRepository.save(imei));
    }

    @Override
    public ImeiDTO update(Integer id, ImeiDTO imeiDTO) {
        return imeiRepository.findById(id).map(existingImei -> {
            existingImei.setImei(imeiDTO.getImei());
            existingImei.setTrangThai(imeiDTO.getTrangThai());
            // Note: ngay_cap_nhat column doesn't exist in the actual database
            // existingImei.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(imeiRepository.save(existingImei));
        }).orElseThrow(() -> new RuntimeException("Imei not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        imeiRepository.deleteById(id);
    }

    @Override
    public List<String> findDuplicateImeis(List<String> imeis) {
        return imeiRepository.findExistingImeis(imeis);
    }
    
    @Override
    public List<ImeiDTO> getByChiTietSanPham(Integer chiTietSanPhamId) {
        List<Imei> imeis = imeiRepository.findByChiTietSanPhamId(chiTietSanPhamId);
        return imeis.stream().map(this::convertToDto).toList();
    }
    
    @Override
    public Imei findByImeiString(String imei) {
        return imeiRepository.findByImei(imei).orElse(null);
    }
    
    @Override
    public ImeiDTO convertToDTO(Imei imei) {
        return convertToDto(imei);
    }

    private ImeiDTO convertToDto(Imei imei) {
        return ImeiDTO.builder()
                .id(imei.getId())
                .imei(imei.getImei())
                .trangThai(imei.getTrangThai())
                .idCtsp(imei.getChiTietSanPham() != null ? imei.getChiTietSanPham().getId() : null)
                .build();
    }

    private Imei convertToEntity(ImeiDTO imeiDTO) {
        Imei imei = Imei.builder()
                .id(imeiDTO.getId())
                .imei(imeiDTO.getImei())
                .trangThai(imeiDTO.getTrangThai())
                .build();

        if (imeiDTO.getIdCtsp() != null) {
            chiTietSanPhamRepository.findById(imeiDTO.getIdCtsp()).ifPresent(imei::setChiTietSanPham);
        }
        return imei;
    }
}


