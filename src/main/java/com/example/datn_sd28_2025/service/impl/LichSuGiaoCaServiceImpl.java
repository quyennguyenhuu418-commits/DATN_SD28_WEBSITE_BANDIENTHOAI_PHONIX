package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.LichSuGiaoCaDTO;
import com.example.datn_sd28_2025.entity.LichSuGiaoCa;
import com.example.datn_sd28_2025.repository.LichSuGiaoCaRepository;
import com.example.datn_sd28_2025.service.LichSuGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LichSuGiaoCaServiceImpl implements LichSuGiaoCaService {

    @Autowired
    private LichSuGiaoCaRepository lichSuGiaoCaRepository;

    @Override
    public List<LichSuGiaoCaDTO> getAll() {
        return lichSuGiaoCaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public LichSuGiaoCaDTO getById(Integer id) {
        return lichSuGiaoCaRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new RuntimeException("Lịch sử giao ca không tồn tại"));
    }

    @Override
    @Transactional
    public LichSuGiaoCaDTO save(LichSuGiaoCaDTO lichSuGiaoCaDTO) {
        LichSuGiaoCa lichSuGiaoCa = convertToEntity(lichSuGiaoCaDTO);
        LichSuGiaoCa savedLichSuGiaoCa = lichSuGiaoCaRepository.save(lichSuGiaoCa);
        return convertToDto(savedLichSuGiaoCa);
    }

    @Override
    @Transactional
    public LichSuGiaoCaDTO update(Integer id, LichSuGiaoCaDTO lichSuGiaoCaDTO) {
        LichSuGiaoCa existingLichSuGiaoCa = lichSuGiaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lịch sử giao ca không tồn tại"));

        // Update fields
        existingLichSuGiaoCa.setGiaoCaId(lichSuGiaoCaDTO.getGiaoCaId());
        existingLichSuGiaoCa.setHanhDong(lichSuGiaoCaDTO.getHanhDong());
        existingLichSuGiaoCa.setNguoiThucHienId(lichSuGiaoCaDTO.getNguoiThucHienId());
        existingLichSuGiaoCa.setNoiDungThayDoi(lichSuGiaoCaDTO.getNoiDungThayDoi());
        existingLichSuGiaoCa.setThoiGian(lichSuGiaoCaDTO.getThoiGian());

        LichSuGiaoCa updatedLichSuGiaoCa = lichSuGiaoCaRepository.save(existingLichSuGiaoCa);
        return convertToDto(updatedLichSuGiaoCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!lichSuGiaoCaRepository.existsById(id)) {
            throw new RuntimeException("Lịch sử giao ca không tồn tại");
        }
        lichSuGiaoCaRepository.deleteById(id);
    }

    @Override
    public List<LichSuGiaoCaDTO> getByGiaoCaId(Integer giaoCaId) {
        return lichSuGiaoCaRepository.findByGiaoCaId(giaoCaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByNguoiThucHienId(Integer nguoiThucHienId) {
        return lichSuGiaoCaRepository.findByNguoiThucHienId(nguoiThucHienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByHanhDong(String hanhDong) {
        return lichSuGiaoCaRepository.findByHanhDong(hanhDong).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByGiaoCaIdOrderByThoiGianDesc(Integer giaoCaId) {
        return lichSuGiaoCaRepository.findByGiaoCaIdOrderByThoiGianDesc(giaoCaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByNguoiThucHienIdOrderByThoiGianDesc(Integer nguoiThucHienId) {
        return lichSuGiaoCaRepository.findByNguoiThucHienIdOrderByThoiGianDesc(nguoiThucHienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return lichSuGiaoCaRepository.findByDateRange(startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByGiaoCaIdAndHanhDong(Integer giaoCaId, String hanhDong) {
        return lichSuGiaoCaRepository.findByGiaoCaIdAndHanhDong(giaoCaId, hanhDong).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByNguoiThucHienIdAndDateRange(Integer nguoiThucHienId, LocalDateTime startDate, LocalDateTime endDate) {
        return lichSuGiaoCaRepository.findByNguoiThucHienIdAndDateRange(nguoiThucHienId, startDate, endDate).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByNhanVienId(Integer nhanVienId) {
        return lichSuGiaoCaRepository.findByNhanVienId(nhanVienId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<LichSuGiaoCaDTO> getByCaId(Integer caId) {
        return lichSuGiaoCaRepository.findByCaId(caId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public LichSuGiaoCaDTO logAction(Integer giaoCaId, String hanhDong, Integer nguoiThucHienId, String noiDungThayDoi) {
        LichSuGiaoCaDTO lichSuDTO = LichSuGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .hanhDong(hanhDong)
                .nguoiThucHienId(nguoiThucHienId)
                .noiDungThayDoi(noiDungThayDoi)
                .thoiGian(LocalDateTime.now())
                .build();
        return save(lichSuDTO);
    }

    private LichSuGiaoCaDTO convertToDto(LichSuGiaoCa lichSuGiaoCa) {
        return LichSuGiaoCaDTO.builder()
                .id(lichSuGiaoCa.getId())
                .giaoCaId(lichSuGiaoCa.getGiaoCaId())
                .hanhDong(lichSuGiaoCa.getHanhDong())
                .nguoiThucHienId(lichSuGiaoCa.getNguoiThucHienId())
                .noiDungThayDoi(lichSuGiaoCa.getNoiDungThayDoi())
                .thoiGian(lichSuGiaoCa.getThoiGian())
                .build();
    }

    private LichSuGiaoCa convertToEntity(LichSuGiaoCaDTO lichSuGiaoCaDTO) {
        return LichSuGiaoCa.builder()
                .id(lichSuGiaoCaDTO.getId())
                .giaoCaId(lichSuGiaoCaDTO.getGiaoCaId())
                .hanhDong(lichSuGiaoCaDTO.getHanhDong())
                .nguoiThucHienId(lichSuGiaoCaDTO.getNguoiThucHienId())
                .noiDungThayDoi(lichSuGiaoCaDTO.getNoiDungThayDoi())
                .thoiGian(lichSuGiaoCaDTO.getThoiGian())
                .build();
    }
}




























