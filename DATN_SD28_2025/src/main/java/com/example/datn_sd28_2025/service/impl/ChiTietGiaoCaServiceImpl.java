package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.ChiTietGiaoCaDTO;
import com.example.datn_sd28_2025.entity.ChiTietGiaoCa;
import com.example.datn_sd28_2025.repository.ChiTietGiaoCaRepository;
import com.example.datn_sd28_2025.service.ChiTietGiaoCaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ChiTietGiaoCaServiceImpl implements ChiTietGiaoCaService {

    @Autowired
    private ChiTietGiaoCaRepository chiTietGiaoCaRepository;

    @Override
    public List<ChiTietGiaoCaDTO> getAll() {
        return chiTietGiaoCaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<ChiTietGiaoCaDTO> getById(Integer id) {
        return chiTietGiaoCaRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO save(ChiTietGiaoCaDTO chiTietGiaoCaDTO) {
        ChiTietGiaoCa chiTietGiaoCa = convertToEntity(chiTietGiaoCaDTO);
        ChiTietGiaoCa savedChiTietGiaoCa = chiTietGiaoCaRepository.save(chiTietGiaoCa);
        return convertToDto(savedChiTietGiaoCa);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO update(Integer id, ChiTietGiaoCaDTO chiTietGiaoCaDTO) {
        ChiTietGiaoCa existingChiTietGiaoCa = chiTietGiaoCaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Chi tiết giao ca không tồn tại"));

        // Update fields
        existingChiTietGiaoCa.setGiaoCaId(chiTietGiaoCaDTO.getGiaoCaId());
        existingChiTietGiaoCa.setLoaiChiTiet(chiTietGiaoCaDTO.getLoaiChiTiet());
        existingChiTietGiaoCa.setTenChiTiet(chiTietGiaoCaDTO.getTenChiTiet());
        existingChiTietGiaoCa.setGiaTri(chiTietGiaoCaDTO.getGiaTri());
        existingChiTietGiaoCa.setSoLuong(chiTietGiaoCaDTO.getSoLuong());
        existingChiTietGiaoCa.setMoTa(chiTietGiaoCaDTO.getMoTa());
        existingChiTietGiaoCa.setThoiGian(chiTietGiaoCaDTO.getThoiGian());
        existingChiTietGiaoCa.setTrangThai(chiTietGiaoCaDTO.getTrangThai());

        ChiTietGiaoCa updatedChiTietGiaoCa = chiTietGiaoCaRepository.save(existingChiTietGiaoCa);
        return convertToDto(updatedChiTietGiaoCa);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!chiTietGiaoCaRepository.existsById(id)) {
            throw new RuntimeException("Chi tiết giao ca không tồn tại");
        }
        chiTietGiaoCaRepository.deleteById(id);
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByGiaoCaId(Integer giaoCaId) {
        return chiTietGiaoCaRepository.findByGiaoCaId(giaoCaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByLoaiChiTiet(String loaiChiTiet) {
        return chiTietGiaoCaRepository.findByLoaiChiTiet(loaiChiTiet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.findByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByTrangThai(Integer trangThai) {
        return chiTietGiaoCaRepository.findByTrangThai(trangThai).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getActiveByGiaoCaId(Integer giaoCaId) {
        return chiTietGiaoCaRepository.findActiveByGiaoCaId(giaoCaId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getActiveByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.findActiveByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByNhanVienIdAndLoaiChiTiet(Integer nhanVienId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.findByNhanVienIdAndLoaiChiTiet(nhanVienId, loaiChiTiet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ChiTietGiaoCaDTO> getByCaIdAndLoaiChiTiet(Integer caId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.findByCaIdAndLoaiChiTiet(caId, loaiChiTiet).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public BigDecimal getTotalValueByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.getTotalValueByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
    }

    @Override
    public Long getTotalQuantityByGiaoCaIdAndLoaiChiTiet(Integer giaoCaId, String loaiChiTiet) {
        return chiTietGiaoCaRepository.getTotalQuantityByGiaoCaIdAndLoaiChiTiet(giaoCaId, loaiChiTiet);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO addDoanhThu(Integer giaoCaId, String tenChiTiet, BigDecimal giaTri, String moTa) {
        ChiTietGiaoCaDTO chiTietDTO = ChiTietGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .loaiChiTiet("DOANH_THU")
                .tenChiTiet(tenChiTiet)
                .giaTri(giaTri)
                .moTa(moTa)
                .thoiGian(LocalDateTime.now())
                .trangThai(1)
                .build();
        return save(chiTietDTO);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO addDonHang(Integer giaoCaId, String tenChiTiet, Integer soLuong, String moTa) {
        ChiTietGiaoCaDTO chiTietDTO = ChiTietGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .loaiChiTiet("DON_HANG")
                .tenChiTiet(tenChiTiet)
                .soLuong(soLuong)
                .moTa(moTa)
                .thoiGian(LocalDateTime.now())
                .trangThai(1)
                .build();
        return save(chiTietDTO);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO addTienMat(Integer giaoCaId, String tenChiTiet, BigDecimal giaTri, String moTa) {
        ChiTietGiaoCaDTO chiTietDTO = ChiTietGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .loaiChiTiet("TIEN_MAT")
                .tenChiTiet(tenChiTiet)
                .giaTri(giaTri)
                .moTa(moTa)
                .thoiGian(LocalDateTime.now())
                .trangThai(1)
                .build();
        return save(chiTietDTO);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO addSuCo(Integer giaoCaId, String tenChiTiet, String moTa) {
        ChiTietGiaoCaDTO chiTietDTO = ChiTietGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .loaiChiTiet("SU_CO")
                .tenChiTiet(tenChiTiet)
                .moTa(moTa)
                .thoiGian(LocalDateTime.now())
                .trangThai(1)
                .build();
        return save(chiTietDTO);
    }

    @Override
    @Transactional
    public ChiTietGiaoCaDTO addCongViec(Integer giaoCaId, String tenChiTiet, String moTa) {
        ChiTietGiaoCaDTO chiTietDTO = ChiTietGiaoCaDTO.builder()
                .giaoCaId(giaoCaId)
                .loaiChiTiet("CONG_VIEC")
                .tenChiTiet(tenChiTiet)
                .moTa(moTa)
                .thoiGian(LocalDateTime.now())
                .trangThai(1)
                .build();
        return save(chiTietDTO);
    }

    private ChiTietGiaoCaDTO convertToDto(ChiTietGiaoCa chiTietGiaoCa) {
        return ChiTietGiaoCaDTO.builder()
                .id(chiTietGiaoCa.getId())
                .giaoCaId(chiTietGiaoCa.getGiaoCaId())
                .loaiChiTiet(chiTietGiaoCa.getLoaiChiTiet())
                .tenChiTiet(chiTietGiaoCa.getTenChiTiet())
                .giaTri(chiTietGiaoCa.getGiaTri())
                .soLuong(chiTietGiaoCa.getSoLuong())
                .moTa(chiTietGiaoCa.getMoTa())
                .thoiGian(chiTietGiaoCa.getThoiGian())
                .trangThai(chiTietGiaoCa.getTrangThai())
                .trangThaiText(getTrangThaiText(chiTietGiaoCa.getTrangThai()))
                .ngayTao(chiTietGiaoCa.getNgayTao())
                .ngayCapNhat(chiTietGiaoCa.getNgayCapNhat())
                .build();
    }

    private ChiTietGiaoCa convertToEntity(ChiTietGiaoCaDTO chiTietGiaoCaDTO) {
        return ChiTietGiaoCa.builder()
                .id(chiTietGiaoCaDTO.getId())
                .giaoCaId(chiTietGiaoCaDTO.getGiaoCaId())
                .loaiChiTiet(chiTietGiaoCaDTO.getLoaiChiTiet())
                .tenChiTiet(chiTietGiaoCaDTO.getTenChiTiet())
                .giaTri(chiTietGiaoCaDTO.getGiaTri())
                .soLuong(chiTietGiaoCaDTO.getSoLuong())
                .moTa(chiTietGiaoCaDTO.getMoTa())
                .thoiGian(chiTietGiaoCaDTO.getThoiGian())
                .trangThai(chiTietGiaoCaDTO.getTrangThai())
                .build();
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        return switch (trangThai) {
            case 1 -> "Hoạt động";
            case 0 -> "Tạm dừng";
            default -> "Không xác định";
        };
    }
}











































