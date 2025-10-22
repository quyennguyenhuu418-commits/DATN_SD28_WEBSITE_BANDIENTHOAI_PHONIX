package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.NhanVienDTO;
import com.example.datn_sd28_2025.entity.NhanVien;
import com.example.datn_sd28_2025.repository.NhanVienRepository;
import com.example.datn_sd28_2025.service.EmailService;
import com.example.datn_sd28_2025.service.NhanVienService;
import com.example.datn_sd28_2025.service.NotificationService;
import com.example.datn_sd28_2025.util.StaffCodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class NhanVienServiceImpl implements NhanVienService {

    @Autowired
    private NhanVienRepository nhanVienRepository;
    
    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<NhanVienDTO> getAll() {
        return nhanVienRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<NhanVienDTO> getById(Integer id) {
        return nhanVienRepository.findById(id)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public NhanVienDTO save(NhanVienDTO nhanVienDTO) {
        // Check if CCCD already exists
        if (nhanVienDTO.getCccd() != null && !nhanVienDTO.getCccd().trim().isEmpty()) {
            if (nhanVienRepository.existsByCccd(nhanVienDTO.getCccd())) {
                throw new RuntimeException("Số căn cước đã tồn tại trong hệ thống");
            }
        }
        
        // Check if email already exists
        if (nhanVienRepository.findByEmail(nhanVienDTO.getEmail()).isPresent()) {
            throw new RuntimeException("Email đã tồn tại trong hệ thống");
        }
        
        // Check if phone number already exists
        if (nhanVienRepository.findBySoDienThoai(nhanVienDTO.getSoDienThoai()).isPresent()) {
            throw new RuntimeException("Số điện thoại đã tồn tại trong hệ thống");
        }
        
        // Check if username already exists
        if (nhanVienRepository.findByTaiKhoan(nhanVienDTO.getTaiKhoan()).isPresent()) {
            throw new RuntimeException("Tài khoản đã tồn tại trong hệ thống");
        }

        NhanVien nhanVien = convertToEntity(nhanVienDTO);
        
        // Ensure password has {noop} prefix
        nhanVien.setMatKhau(ensureNoopPrefix(nhanVien.getMatKhau()));
        
        // Generate maNhanVien if not provided
        if (nhanVien.getMaNhanVien() == null || nhanVien.getMaNhanVien().trim().isEmpty()) {
            nhanVien.setMaNhanVien(StaffCodeGenerator.generateStaffCode(nhanVien.getHoTen()));
        }
        
        // Set creation time
        nhanVien.setNgayTao(LocalDateTime.now());
        nhanVien.setNgayCapNhat(LocalDateTime.now());
        
        // Set default status if not provided
        if (nhanVien.getTrangThai() == null) {
            nhanVien.setTrangThai(1); // Active
        }

        NhanVien savedNhanVien = nhanVienRepository.save(nhanVien);
        
        // Send email notification
        try {
            emailService.sendEmployeeAccountInfo(
                savedNhanVien.getEmail(),
                savedNhanVien.getHoTen(),
                savedNhanVien.getTaiKhoan(),
                savedNhanVien.getMatKhau(),
                savedNhanVien.getChucVu()
            );
        } catch (Exception e) {
            System.err.println("Error sending email notification: " + e.getMessage());
            // Don't fail the save operation if email fails
        }

        // Tạo thông báo cho nhân viên mới
        try {
            notificationService.createEmployeeNotification(
                savedNhanVien.getHoTen(), 
                savedNhanVien.getChucVu(),
                savedNhanVien.getEmail()
            );
        } catch (Exception e) {
            // Log error but don't fail the employee creation
            System.err.println("Error creating employee notification: " + e.getMessage());
        }

        return convertToDto(savedNhanVien);
    }

    @Override
    @Transactional
    public NhanVienDTO update(Integer id, NhanVienDTO nhanVienDTO) {
        NhanVien existingNhanVien = nhanVienRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nhân viên không tồn tại"));

        // Check if CCCD already exists for another employee
        if (nhanVienDTO.getCccd() != null && !nhanVienDTO.getCccd().trim().isEmpty()) {
            Optional<NhanVien> existingByCccd = nhanVienRepository.findByCccd(nhanVienDTO.getCccd());
            if (existingByCccd.isPresent() && !existingByCccd.get().getId().equals(id)) {
                throw new RuntimeException("Số căn cước đã tồn tại cho nhân viên khác");
            }
        }
        
        // Check if email already exists for another employee
        Optional<NhanVien> existingByEmail = nhanVienRepository.findByEmail(nhanVienDTO.getEmail());
        if (existingByEmail.isPresent() && !existingByEmail.get().getId().equals(id)) {
            throw new RuntimeException("Email đã tồn tại cho nhân viên khác");
        }
        
        // Check if phone number already exists for another employee
        Optional<NhanVien> existingByPhone = nhanVienRepository.findBySoDienThoai(nhanVienDTO.getSoDienThoai());
        if (existingByPhone.isPresent() && !existingByPhone.get().getId().equals(id)) {
            throw new RuntimeException("Số điện thoại đã tồn tại cho nhân viên khác");
        }
        
        // Check if username already exists for another employee
        Optional<NhanVien> existingByUsername = nhanVienRepository.findByTaiKhoan(nhanVienDTO.getTaiKhoan());
        if (existingByUsername.isPresent() && !existingByUsername.get().getId().equals(id)) {
            throw new RuntimeException("Tài khoản đã tồn tại cho nhân viên khác");
        }

        // Update fields
        existingNhanVien.setHoTen(nhanVienDTO.getHoTen());
        existingNhanVien.setCccd(nhanVienDTO.getCccd());
        existingNhanVien.setSoDienThoai(nhanVienDTO.getSoDienThoai());
        existingNhanVien.setNgaySinh(nhanVienDTO.getNgaySinh());
        existingNhanVien.setTaiKhoan(nhanVienDTO.getTaiKhoan());
        existingNhanVien.setMatKhau(ensureNoopPrefix(nhanVienDTO.getMatKhau()));
        existingNhanVien.setGioiTinh(nhanVienDTO.getGioiTinh());
        existingNhanVien.setDiaChi(nhanVienDTO.getDiaChi());
        existingNhanVien.setEmail(nhanVienDTO.getEmail());
        existingNhanVien.setChucVu(nhanVienDTO.getChucVu());
        existingNhanVien.setAnhDaiDien(nhanVienDTO.getAnhDaiDien());
        existingNhanVien.setNgayCapNhat(LocalDateTime.now());
        
        if (nhanVienDTO.getTrangThai() != null) {
            existingNhanVien.setTrangThai(nhanVienDTO.getTrangThai());
        }

        NhanVien updatedNhanVien = nhanVienRepository.save(existingNhanVien);
        return convertToDto(updatedNhanVien);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!nhanVienRepository.existsById(id)) {
            throw new RuntimeException("Nhân viên không tồn tại");
        }
        nhanVienRepository.deleteById(id);
    }

    @Override
    public List<NhanVienDTO> searchByQuery(String query) {
        return nhanVienRepository.findByHoTenContaining(query).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NhanVienDTO> findByChucVu(String chucVu) {
        return nhanVienRepository.findByChucVu(chucVu).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<NhanVienDTO> findAllActive() {
        return nhanVienRepository.findAllActive().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NhanVienDTO convertToDto(NhanVien nhanVien) {
        return NhanVienDTO.builder()
                .id(nhanVien.getId())
                .maNhanVien(nhanVien.getMaNhanVien())
                .hoTen(nhanVien.getHoTen())
                .cccd(nhanVien.getCccd())
                .soDienThoai(nhanVien.getSoDienThoai())
                .ngaySinh(nhanVien.getNgaySinh())
                .taiKhoan(nhanVien.getTaiKhoan())
                .matKhau(nhanVien.getMatKhau())
                .gioiTinh(nhanVien.getGioiTinh())
                .diaChi(nhanVien.getDiaChi())
                .email(nhanVien.getEmail())
                .chucVu(nhanVien.getChucVu())
                .anhDaiDien(nhanVien.getAnhDaiDien())
                .ngayTao(nhanVien.getNgayTao())
                .ngayCapNhat(nhanVien.getNgayCapNhat())
                .trangThai(nhanVien.getTrangThai())
                .build();
    }

    private NhanVien convertToEntity(NhanVienDTO nhanVienDTO) {
        return NhanVien.builder()
                .id(nhanVienDTO.getId())
                .maNhanVien(nhanVienDTO.getMaNhanVien())
                .hoTen(nhanVienDTO.getHoTen())
                .cccd(nhanVienDTO.getCccd())
                .soDienThoai(nhanVienDTO.getSoDienThoai())
                .ngaySinh(nhanVienDTO.getNgaySinh())
                .taiKhoan(nhanVienDTO.getTaiKhoan())
                .matKhau(nhanVienDTO.getMatKhau())
                .gioiTinh(nhanVienDTO.getGioiTinh())
                .diaChi(nhanVienDTO.getDiaChi())
                .email(nhanVienDTO.getEmail())
                .chucVu(nhanVienDTO.getChucVu())
                .anhDaiDien(nhanVienDTO.getAnhDaiDien())
                .ngayTao(nhanVienDTO.getNgayTao())
                .ngayCapNhat(nhanVienDTO.getNgayCapNhat())
                .trangThai(nhanVienDTO.getTrangThai())
                .build();
    }


    @Override
    public boolean existsByCccd(String cccd) {
        return nhanVienRepository.existsByCccd(cccd);
    }
    
    /**
     * Thêm prefix {noop} vào mật khẩu nếu chưa có
     */
    private String ensureNoopPrefix(String password) {
        if (password == null || password.trim().isEmpty()) {
            return password;
        }
        
        // Nếu mật khẩu đã có prefix, giữ nguyên
        if (password.startsWith("{") && password.contains("}")) {
            return password;
        }
        
        // Thêm prefix {noop} cho plain text password
        return "{noop}" + password;
    }


    private boolean isAdminAccount(NhanVien nhanVien) {
        if (nhanVien.getChucVu() == null) return false;
        
        String chucVu = nhanVien.getChucVu().toLowerCase().trim();
        return chucVu.contains("quản trị") || 
               chucVu.contains("admin") || 
               chucVu.contains("administrator") ||
               chucVu.contains("giám đốc") ||
               chucVu.contains("director");
    }

    private String determineRoleByPosition(String chucVu) {
        if (chucVu == null || chucVu.trim().isEmpty()) {
            return "Nhân viên";
        }
        
        String chucVuLower = chucVu.toLowerCase().trim();
        
        // Admin positions
        if (chucVuLower.contains("quản trị") || chucVuLower.contains("admin") || chucVuLower.contains("administrator")) {
            return "Quản trị viên";
        }
        
        // Manager positions
        if (chucVuLower.contains("quản lý") || chucVuLower.contains("manager") || 
            chucVuLower.contains("giám đốc") || chucVuLower.contains("director") || 
            chucVuLower.contains("trưởng phòng")) {
            return "Quản lý";
        }
        
        // Staff positions
        return "Nhân viên";
    }
}





