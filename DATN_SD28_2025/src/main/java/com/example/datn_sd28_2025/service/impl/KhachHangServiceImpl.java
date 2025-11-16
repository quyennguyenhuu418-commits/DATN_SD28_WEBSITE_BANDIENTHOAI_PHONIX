package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.KhachHangDTO;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.entity.UserDiaChi;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.repository.UserDiaChiRepository;
import com.example.datn_sd28_2025.service.KhachHangService;
import com.example.datn_sd28_2025.service.NotificationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KhachHangServiceImpl implements KhachHangService {

    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private UserDiaChiRepository userDiaChiRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public List<KhachHangDTO> getAll() {
        return khachHangRepository.findAll().stream()
                .map(khachHang -> {
                    KhachHangDTO dto = convertToDto(khachHang);
                    // Lấy địa chỉ mặc định cho mỗi khách hàng
                    String defaultAddress = getDefaultAddress(khachHang.getId());
                    dto.setDiaChi(defaultAddress);
                    return dto;
                })
                .toList();
    }

    @Override
    public Optional<KhachHangDTO> getById(Integer id) {
        return khachHangRepository.findById(id)
                .map(khachHang -> {
                    KhachHangDTO dto = convertToDto(khachHang);
                    // Lấy địa chỉ mặc định cho khách hàng
                    String defaultAddress = getDefaultAddress(khachHang.getId());
                    dto.setDiaChi(defaultAddress);
                    return dto;
                });
    }

    @Override
    public KhachHangDTO save(KhachHangDTO khachHangDTO) {
        KhachHang khachHang = convertToEntity(khachHangDTO);
        
        // Tạo mã khách hàng tự động nếu chưa có
        if (khachHang.getMaKhachHang() == null || khachHang.getMaKhachHang().trim().isEmpty()) {
            khachHang.setMaKhachHang(generateMaKhachHang());
        }
        
        khachHang.setNgayTao(LocalDateTime.now());
        khachHang.setNgayCapNhat(LocalDateTime.now());
        if (khachHang.getTrangThai() == null) {
            khachHang.setTrangThai(1); // Active by default
        }
        
        // Tự động set nguoiTao từ SecurityContext (nhân viên đang đăng nhập)
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            khachHang.setNguoiTao(currentUsername);
            khachHang.setNguoiCapNhat(currentUsername);
        }
        
        KhachHang savedKhachHang = khachHangRepository.save(khachHang);
        
        // Tạo thông báo cho khách hàng mới
        try {
            notificationService.createCustomerNotification(
                savedKhachHang.getHoTen(), 
                savedKhachHang.getEmail()
            );
        } catch (Exception e) {
            // Log error but don't fail the customer creation
            System.err.println("Error creating customer notification: " + e.getMessage());
        }
        
        return convertToDto(savedKhachHang);
    }

    @Override
    public KhachHangDTO update(Integer id, KhachHangDTO khachHangDTO) {
        return khachHangRepository.findById(id)
                .map(existingKhachHang -> {
                    // Cho phép cập nhật cả khách hàng hoạt động và không hoạt động
                    existingKhachHang.setHoTen(khachHangDTO.getHoTen());
                    existingKhachHang.setSoDienThoai(khachHangDTO.getSoDienThoai());
                    existingKhachHang.setEmail(khachHangDTO.getEmail());
                    // diaChi removed - address is now managed through user_dia_chi table
                    existingKhachHang.setGioiTinh(khachHangDTO.getGioiTinh());
                    existingKhachHang.setNgaySinh(khachHangDTO.getNgaySinh());
                    existingKhachHang.setTrangThai(khachHangDTO.getTrangThai());
                    // Không cập nhật tài khoản và mật khẩu từ form quản lý
                    // Khách hàng tự đăng ký trên web, không quản lý từ admin
                    existingKhachHang.setNgayCapNhat(LocalDateTime.now());
                    
                    // Tự động set nguoiCapNhat từ SecurityContext (nhân viên đang đăng nhập)
                    String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
                    if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
                        existingKhachHang.setNguoiCapNhat(currentUsername);
                    }
                    
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
    @Transactional
    public boolean updateStatusOnly(Integer id, Integer trangThai) {
        try {
            System.out.println("Updating status for customer " + id + " to " + trangThai);
            
            // Check if customer exists
            if (!khachHangRepository.existsById(id)) {
                System.out.println("Customer " + id + " not found");
                return false;
            }
            
            // Use direct SQL update to avoid validation
            int updated = khachHangRepository.updateStatusOnly(id, trangThai, LocalDateTime.now());
            
            System.out.println("Updated " + updated + " records for customer " + id);
            return updated > 0;
        } catch (Exception e) {
            System.err.println("Error updating status for customer " + id + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
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
                .filter(khachHang -> khachHang.getTrangThai() == 1)
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<KhachHangDTO> findBySoDienThoaiContaining(String soDienThoai) {
        return khachHangRepository.findBySoDienThoaiContaining(soDienThoai)
                .stream()
                .filter(khachHang -> khachHang.getTrangThai() == 1)
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Long countActiveCustomers() {
        return khachHangRepository.countActiveCustomers();
    }

    @Override
    public List<KhachHangDTO> getActiveCustomers() {
        return khachHangRepository.findByTrangThai(1)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Optional<KhachHangDTO> findByEmail(String email) {
        try {
            KhachHang customer = khachHangRepository.findByEmail(email);
            if (customer == null) {
                return Optional.empty();
            }
            return Optional.of(convertToDto(customer));
        } catch (Exception e) {
            System.err.println("Error finding customer by email: " + email + ", Error: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public Optional<KhachHangDTO> findBySoDienThoai(String soDienThoai) {
        try {
            Optional<KhachHang> customerOpt = khachHangRepository.findBySoDienThoai(soDienThoai);
            if (customerOpt.isEmpty()) {
                return Optional.empty();
            }
            return customerOpt.map(this::convertToDto);
        } catch (Exception e) {
            System.err.println("Error finding customer by phone: " + soDienThoai + ", Error: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

    private KhachHangDTO convertToDto(KhachHang khachHang) {
        return KhachHangDTO.builder()
                .id(khachHang.getId())
                .maKhachHang(khachHang.getMaKhachHang())
                .hoTen(khachHang.getHoTen())
                .soDienThoai(khachHang.getSoDienThoai())
                .ngaySinh(khachHang.getNgaySinh())
                .gioiTinh(khachHang.getGioiTinh())
                .email(khachHang.getEmail())
                // Address information is now managed through user_dia_chi table
                .taiKhoan(khachHang.getTaiKhoan())
                .matKhau(khachHang.getMatKhau())
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
                .ngaySinh(khachHangDTO.getNgaySinh())
                .gioiTinh(khachHangDTO.getGioiTinh())
                .email(khachHangDTO.getEmail())
                // Address information is now managed through user_dia_chi table
                .taiKhoan(khachHangDTO.getTaiKhoan())
                .matKhau(khachHangDTO.getMatKhau())
                .ngayTao(khachHangDTO.getNgayTao())
                .ngayCapNhat(khachHangDTO.getNgayCapNhat())
                .trangThai(khachHangDTO.getTrangThai())
                .nguoiTao(khachHangDTO.getNguoiTao())
                .nguoiCapNhat(khachHangDTO.getNguoiCapNhat())
                .build();
    }

    /**
     * Get default address for a customer from user_dia_chi table
     */
    public String getDefaultAddress(Integer customerId) {
        try {
            // Find the default address for this customer
            UserDiaChi defaultAddress = userDiaChiRepository.findDiaChiMacDinhByKhachHangId(customerId);
            if (defaultAddress != null && defaultAddress.getDiaChi() != null) {
                // Build full address string from DiaChi entity
                StringBuilder addressBuilder = new StringBuilder();
                
                // Add detailed address first
                if (defaultAddress.getDiaChi().getDiaChiChiTiet() != null && !defaultAddress.getDiaChi().getDiaChiChiTiet().trim().isEmpty()) {
                    addressBuilder.append(defaultAddress.getDiaChi().getDiaChiChiTiet());
                }
                
                // Only add phuong_xa if it's not already included in dia_chi_chi_tiet
                if (defaultAddress.getDiaChi().getPhuongXa() != null && !defaultAddress.getDiaChi().getPhuongXa().trim().isEmpty()) {
                    String phuongXa = defaultAddress.getDiaChi().getPhuongXa();
                    String diaChiChiTiet = defaultAddress.getDiaChi().getDiaChiChiTiet();
                    
                    // Check if phuong_xa is already in dia_chi_chi_tiet to avoid duplication
                    if (diaChiChiTiet == null || !diaChiChiTiet.contains(phuongXa)) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(phuongXa);
                    }
                }
                
                // Only add tinh_thanh_pho if it's not already included in the address
                if (defaultAddress.getDiaChi().getTinhThanhPho() != null && !defaultAddress.getDiaChi().getTinhThanhPho().trim().isEmpty()) {
                    String tinhThanhPho = defaultAddress.getDiaChi().getTinhThanhPho();
                    String currentAddress = addressBuilder.toString();
                    
                    // Check if tinh_thanh_pho is already in the address to avoid duplication
                    if (!currentAddress.contains(tinhThanhPho)) {
                        if (addressBuilder.length() > 0) addressBuilder.append(", ");
                        addressBuilder.append(tinhThanhPho);
                    }
                }
                
                return addressBuilder.toString();
            }
        } catch (Exception e) {
            System.err.println("Error getting default address for customer " + customerId + ": " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Tạo mã khách hàng tự động theo format: KH + YYYY + 4 số thứ tự
     * Ví dụ: KH20250001, KH20250002, ...
     */
    private String generateMaKhachHang() {
        try {
            String currentYear = String.valueOf(LocalDateTime.now().getYear());
            String prefix = "KH" + currentYear;
            int prefixLength = prefix.length(); // 6 ký tự (KH + 4 số năm)
            
            // Tìm mã khách hàng mới nhất theo prefix
            Optional<KhachHang> latestCustomerOpt = khachHangRepository
                    .findTopByMaKhachHangStartingWithOrderByMaKhachHangDesc(prefix);

            long nextSequence = 1;
            if (latestCustomerOpt.isPresent()) {
                String latestCode = latestCustomerOpt.get().getMaKhachHang();
                if (latestCode != null && latestCode.length() > prefixLength) {
                    String sequencePart = latestCode.substring(prefixLength);
                    try {
                        nextSequence = Long.parseLong(sequencePart) + 1;
                    } catch (NumberFormatException e) {
                        System.err.println("Invalid sequence format in maKhachHang: " + latestCode);
                        nextSequence = 1;
                    }
                }
            }
            
            // Format số thứ tự với 4 chữ số
            String sequence = String.format("%04d", nextSequence);
            
            return prefix + sequence;
        } catch (Exception e) {
            System.err.println("Error generating maKhachHang: " + e.getMessage());
            e.printStackTrace();
            // Fallback: sử dụng timestamp
            return "KH" + System.currentTimeMillis();
        }
    }
}
