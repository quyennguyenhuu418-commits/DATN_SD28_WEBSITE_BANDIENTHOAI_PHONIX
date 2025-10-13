package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.KhachHangDTO;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Override
    public List<KhachHangDTO> getAll() {
        return khachHangRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Optional<KhachHangDTO> getById(Integer id) {
        return khachHangRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    public KhachHangDTO save(KhachHangDTO khachHangDTO) {
        KhachHang khachHang = convertToEntity(khachHangDTO);
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setNgayCapNhat(LocalDateTime.now());
        if (khachHang.getTrangThai() == null) {
            khachHang.setTrangThai(1); // Active by default
        }
        return convertToDto(khachHangRepository.save(khachHang));
    }

    @Override
    public KhachHangDTO update(Integer id, KhachHangDTO khachHangDTO) {
        return khachHangRepository.findById(id)
                .map(existingKhachHang -> {
                    existingKhachHang.setHoTen(khachHangDTO.getHoTen());
                    existingKhachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
                    existingKhachHang.setEmail(khachHangDTO.getEmail());
                    existingKhachHang.setDiaChi(khachHangDTO.getDiaChi());
                    existingKhachHang.setGioiTinh(khachHangDTO.getGioiTinh());
                    existingKhachHang.setNgaySinh(khachHangDTO.getNgaySinh());
                    existingKhachHang.setTrangThai(khachHangDTO.getTrangThai());
                    existingKhachHang.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(khachHangRepository.save(existingKhachHang));
                })
                .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại với id: " + id));
    }

    @Override
    public void delete(Integer id) {
        if (!khachHangRepository.existsById(id)) {
            throw new RuntimeException("Khách hàng không tồn tại với id: " + id);
        }
        khachHangRepository.deleteById(id);
    }

    @Override
    public List<KhachHangDTO> searchByQuery(String query) {
        return khachHangRepository.findByQuery(query)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<KhachHangDTO> findByHoTenContaining(String hoTen) {
        return khachHangRepository.findByHoTenContaining(hoTen)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<KhachHangDTO> findBySoDienThoaiContaining(String soDienThoai) {
        return khachHangRepository.findBySoDienThoaiContaining(soDienThoai)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Long countActiveCustomers() {
        return khachHangRepository.countActiveCustomers();
    }

    private KhachHangDTO convertToDto(KhachHang khachHang) {
        return KhachHangDTO.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .soDienThoai(khachHang.getSoDienThoai())
                .taiKhoan(khachHang.getTaiKhoan())
                .matKhau(khachHang.getMatKhau())
                .ngaySinh(khachHang.getNgaySinh())
                .gioiTinh(khachHang.getGioiTinh())
                .email(khachHang.getEmail())
                .diaChi(khachHang.getDiaChi())
                .ngayTao(khachHang.getNgayTao())
                .ngayCapNhat(khachHang.getNgayCapNhat())
                .trangThai(khachHang.getTrangThai())
                .nguoiTao(khachHang.getNguoiTao())
                .nguoiCapNhat(khachHang.getNguoiCapNhat())
                .build();
    }

    private KhachHang convertToEntity(KhachHangDTO khachHangDTO) {
        return KhachHang.builder()
                .id(khachHangDTO.getId())
                .maKhachHang(khachHangDTO.getMaKhachHang())
                .hoTen(khachHangDTO.getHoTen())
                .soDienThoai(khachHangDTO.getSoDienThoai())
                .taiKhoan(khachHangDTO.getTaiKhoan())
                .matKhau(khachHangDTO.getMatKhau())
                .ngaySinh(khachHangDTO.getNgaySinh())
                .gioiTinh(khachHangDTO.getGioiTinh())
                .email(khachHangDTO.getEmail())
                .diaChi(khachHangDTO.getDiaChi())
                .ngayTao(khachHangDTO.getNgayTao())
                .ngayCapNhat(khachHangDTO.getNgayCapNhat())
                .trangThai(khachHangDTO.getTrangThai())
                .nguoiTao(khachHangDTO.getNguoiTao())
                .nguoiCapNhat(khachHangDTO.getNguoiCapNhat())
                .build();
    }
}
