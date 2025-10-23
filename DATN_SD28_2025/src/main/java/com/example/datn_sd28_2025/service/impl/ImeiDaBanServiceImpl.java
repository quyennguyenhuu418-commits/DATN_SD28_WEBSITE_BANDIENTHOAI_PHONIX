package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.ImeiDaBanDTO;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.Imei;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ImeiDaBanRepository;
import com.example.datn_sd28_2025.repository.ImeiRepository;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImeiDaBanServiceImpl implements ImeiDaBanService {

    @Autowired
    private ImeiDaBanRepository imeiDaBanRepository;

    @Autowired
    private ImeiRepository imeiRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Override
    public ImeiDaBanDTO createImeiDaBan(ImeiDaBanDTO imeiDaBanDTO) {
        ImeiDaBan imeiDaBan = new ImeiDaBan();
        imeiDaBan.setImei(imeiDaBanDTO.getImei());
        imeiDaBan.setTrangThai(1); // Active
        
        if (imeiDaBanDTO.getIdHoaDonChiTiet() != null) {
            HoaDonCt hoaDonCt = hoaDonCtRepository.findById(imeiDaBanDTO.getIdHoaDonChiTiet())
                    .orElseThrow(() -> new RuntimeException("Hóa đơn chi tiết không tồn tại"));
            imeiDaBan.setHoaDonChiTiet(hoaDonCt);
        }
        
        ImeiDaBan saved = imeiDaBanRepository.save(imeiDaBan);
        return convertToDto(saved);
    }

    @Override
    public List<ImeiDaBanDTO> getByHoaDonChiTiet(Integer idHoaDonChiTiet) {
        return imeiDaBanRepository.findByIdHoaDonChiTiet(idHoaDonChiTiet)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public ImeiDaBanDTO getByImei(String imei) {
        List<ImeiDaBan> imeiDaBanList = imeiDaBanRepository.findByImei(imei);
        if (imeiDaBanList != null && !imeiDaBanList.isEmpty()) {
            // Trả về bản ghi đầu tiên
            return convertToDto(imeiDaBanList.get(0));
        }
        return null;
    }

    @Override
    public List<ImeiDaBanDTO> getAllActive() {
        return imeiDaBanRepository.findAllActive()
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    @Transactional
    public void markImeiAsSold(String imei, Integer idHoaDonChiTiet) {
        // Find the IMEI in the available list
        Imei imeiEntity = imeiRepository.findByImei(imei)
                .orElseThrow(() -> new RuntimeException("IMEI không tồn tại: " + imei));
        
        if (imeiEntity.getTrangThai() != 1) {
            throw new RuntimeException("IMEI đã được bán hoặc không khả dụng: " + imei);
        }
        
        // Mark IMEI as sold (trangThai = 0)
        imeiEntity.setTrangThai(0);
        imeiRepository.save(imeiEntity);
        
        // Create ImeiDaBan record
        HoaDonCt hoaDonCt = hoaDonCtRepository.findById(idHoaDonChiTiet)
                .orElseThrow(() -> new RuntimeException("Hóa đơn chi tiết không tồn tại"));
        
        ImeiDaBan imeiDaBan = new ImeiDaBan();
        imeiDaBan.setHoaDonChiTiet(hoaDonCt);
        imeiDaBan.setImei(imei);
        imeiDaBan.setTrangThai(1); // Active
        imeiDaBanRepository.save(imeiDaBan);
    }

    private ImeiDaBanDTO convertToDto(ImeiDaBan imeiDaBan) {
        ImeiDaBanDTO dto = new ImeiDaBanDTO();
        dto.setId(imeiDaBan.getId());
        dto.setImei(imeiDaBan.getImei());
        dto.setTrangThai(imeiDaBan.getTrangThai());
        
        if (imeiDaBan.getHoaDonChiTiet() != null) {
            dto.setIdHoaDonChiTiet(imeiDaBan.getHoaDonChiTiet().getId());
        }
        
        return dto;
    }
}
