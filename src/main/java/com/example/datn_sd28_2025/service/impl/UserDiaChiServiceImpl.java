package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.DiaChiDTO;
import com.example.datn_sd28_2025.dto.KhachHangDTO;
import com.example.datn_sd28_2025.dto.UserDiaChiDTO;
import com.example.datn_sd28_2025.entity.DiaChi;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.entity.UserDiaChi;
import com.example.datn_sd28_2025.repository.DiaChiRepository;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.repository.UserDiaChiRepository;
import com.example.datn_sd28_2025.service.UserDiaChiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDiaChiServiceImpl implements UserDiaChiService {
    
    private final UserDiaChiRepository userDiaChiRepository;
    private final KhachHangRepository khachHangRepository;
    private final DiaChiRepository diaChiRepository;
    
    @Override
    public List<UserDiaChiDTO> getByKhachHangId(Integer idKhachHang) {
        return userDiaChiRepository.findByKhachHangIdAndTrangThai(idKhachHang)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    @Override
    public UserDiaChiDTO getDiaChiMacDinhByKhachHangId(Integer idKhachHang) {
        UserDiaChi userDiaChi = userDiaChiRepository.findDiaChiMacDinhByKhachHangId(idKhachHang);
        return userDiaChi != null ? convertToDto(userDiaChi) : null;
    }
    
    @Override
    public List<UserDiaChiDTO> getByKhachHangIdAndLoaiDiaChi(Integer idKhachHang, String loaiDiaChi) {
        return userDiaChiRepository.findByKhachHangIdAndLoaiDiaChi(idKhachHang, loaiDiaChi)
                .stream()
                .map(this::convertToDto)
                .toList();
    }
    
    @Override
    @Transactional
    public UserDiaChiDTO create(UserDiaChiDTO userDiaChiDTO) {
        UserDiaChi userDiaChi = convertToEntity(userDiaChiDTO);
        userDiaChi.setNgayTao(LocalDateTime.now());
        userDiaChi.setNgayCapNhat(LocalDateTime.now());
        userDiaChi.setTrangThai(1);
        
        // Nếu đây là địa chỉ mặc định, bỏ mặc định của các địa chỉ khác
        if (Boolean.TRUE.equals(userDiaChi.getMacDinh())) {
            setOtherAddressesNotDefault(userDiaChi.getKhachHang().getId());
        }
        
        return convertToDto(userDiaChiRepository.save(userDiaChi));
    }
    
    @Override
    @Transactional
    public UserDiaChiDTO update(Integer id, UserDiaChiDTO userDiaChiDTO) {
        return userDiaChiRepository.findById(id)
                .map(existingUserDiaChi -> {
                    existingUserDiaChi.setLoaiDiaChi(userDiaChiDTO.getLoaiDiaChi());
                    existingUserDiaChi.setMacDinh(userDiaChiDTO.getMacDinh());
                    existingUserDiaChi.setNgayCapNhat(LocalDateTime.now());
                    
                    // Nếu đây là địa chỉ mặc định, bỏ mặc định của các địa chỉ khác
                    if (Boolean.TRUE.equals(existingUserDiaChi.getMacDinh())) {
                        setOtherAddressesNotDefault(existingUserDiaChi.getKhachHang().getId());
                    }
                    
                    return convertToDto(userDiaChiRepository.save(existingUserDiaChi));
                })
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với id: " + id));
    }
    
    @Override
    @Transactional
    public void delete(Integer id) {
        userDiaChiRepository.findById(id)
                .ifPresentOrElse(
                        userDiaChi -> {
                            userDiaChi.setTrangThai(0);
                            userDiaChi.setNgayCapNhat(LocalDateTime.now());
                            userDiaChiRepository.save(userDiaChi);
                        },
                        () -> {
                            throw new RuntimeException("Địa chỉ không tồn tại với id: " + id);
                        }
                );
    }
    
    @Override
    @Transactional
    public UserDiaChiDTO setDiaChiMacDinh(Integer id) {
        UserDiaChi userDiaChi = userDiaChiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với id: " + id));
        
        System.out.println("=== SETTING DEFAULT ADDRESS START ===");
        System.out.println("UserDiaChi ID: " + userDiaChi.getId());
        System.out.println("KhachHang ID: " + userDiaChi.getKhachHang().getId());
        System.out.println("DiaChi ID: " + (userDiaChi.getDiaChi() != null ? userDiaChi.getDiaChi().getId() : "null"));
        System.out.println("Current macDinh: " + userDiaChi.getMacDinh());
        
        // Bỏ mặc định của các địa chỉ khác
        setOtherAddressesNotDefault(userDiaChi.getKhachHang().getId());
        
        // Đặt địa chỉ này làm mặc định
        userDiaChi.setMacDinh(true);
        userDiaChi.setNgayCapNhat(LocalDateTime.now());
        UserDiaChi savedUserDiaChi = userDiaChiRepository.save(userDiaChi);
        
        System.out.println("✅ UserDiaChi updated - macDinh: " + savedUserDiaChi.getMacDinh());
        
        // Address information is now managed through user_dia_chi table
        // No need to update KhachHang.diaChi field
        System.out.println("✅ UserDiaChi set as default address successfully");
        System.out.println("Address is now managed through user_dia_chi table");
        
        return convertToDto(savedUserDiaChi);
    }
    
    private void setOtherAddressesNotDefault(Integer idKhachHang) {
        List<UserDiaChi> otherAddresses = userDiaChiRepository.findByKhachHangIdAndTrangThai(idKhachHang);
        otherAddresses.forEach(address -> {
            if (Boolean.TRUE.equals(address.getMacDinh())) {
                address.setMacDinh(false);
                address.setNgayCapNhat(LocalDateTime.now());
                userDiaChiRepository.save(address);
            }
        });
    }
    
    private UserDiaChiDTO convertToDto(UserDiaChi userDiaChi) {
        KhachHangDTO khachHangDTO = null;
        if (userDiaChi.getKhachHang() != null) {
            khachHangDTO = KhachHangDTO.builder()
                    .id(userDiaChi.getKhachHang().getId())
                    .hoTen(userDiaChi.getKhachHang().getHoTen())
                    .soDienThoai(userDiaChi.getKhachHang().getSoDienThoai())
                    .build();
        }
        
        DiaChiDTO diaChiDTO = null;
        if (userDiaChi.getDiaChi() != null) {
            diaChiDTO = DiaChiDTO.builder()
                    .id(userDiaChi.getDiaChi().getId())
                    .diaChiChiTiet(userDiaChi.getDiaChi().getDiaChiChiTiet())
                    .phuongXa(userDiaChi.getDiaChi().getPhuongXa())
                    .quanHuyen(userDiaChi.getDiaChi().getQuanHuyen())
                    .tinhThanhPho(userDiaChi.getDiaChi().getTinhThanhPho())
                    .maBuuDien(userDiaChi.getDiaChi().getMaBuuDien())
                    .ghiChu(userDiaChi.getDiaChi().getGhiChu())
                    .ngayTao(userDiaChi.getDiaChi().getNgayTao())
                    .ngayCapNhat(userDiaChi.getDiaChi().getNgayCapNhat())
                    .trangThai(userDiaChi.getDiaChi().getTrangThai())
                    .build();
        }
        
        return UserDiaChiDTO.builder()
                .id(userDiaChi.getId())
                .idDiaChi(userDiaChi.getDiaChi() != null ? userDiaChi.getDiaChi().getId() : null)
                .idUser(userDiaChi.getKhachHang() != null ? userDiaChi.getKhachHang().getId() : null)
                .khachHang(khachHangDTO)
                .diaChi(diaChiDTO)
                .loaiDiaChi(userDiaChi.getLoaiDiaChi())
                .macDinh(userDiaChi.getMacDinh())
                .ngayTao(userDiaChi.getNgayTao())
                .ngayCapNhat(userDiaChi.getNgayCapNhat())
                .trangThai(userDiaChi.getTrangThai())
                .build();
    }
    
    private UserDiaChi convertToEntity(UserDiaChiDTO userDiaChiDTO) {
        KhachHang khachHang = null;
        if (userDiaChiDTO.getIdUser() != null) {
            khachHang = khachHangRepository.findById(userDiaChiDTO.getIdUser())
                    .orElseThrow(() -> new RuntimeException("Khách hàng không tồn tại với id: " + userDiaChiDTO.getIdUser()));
        }
        
        DiaChi diaChi = null;
        if (userDiaChiDTO.getIdDiaChi() != null) {
            diaChi = diaChiRepository.findById(userDiaChiDTO.getIdDiaChi())
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với id: " + userDiaChiDTO.getIdDiaChi()));
        }
        
        return UserDiaChi.builder()
                .id(userDiaChiDTO.getId())
                .khachHang(khachHang)
                .diaChi(diaChi)
                .loaiDiaChi(userDiaChiDTO.getLoaiDiaChi())
                .macDinh(userDiaChiDTO.getMacDinh())
                .ngayTao(userDiaChiDTO.getNgayTao())
                .ngayCapNhat(userDiaChiDTO.getNgayCapNhat())
                .trangThai(userDiaChiDTO.getTrangThai())
                .build();
    }
}
