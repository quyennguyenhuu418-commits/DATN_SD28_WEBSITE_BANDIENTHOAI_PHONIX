package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.PhieuGiamGiaDTO;
import com.example.datn_sd28_2025.entity.PhieuGiamGia;
import com.example.datn_sd28_2025.entity.KhachHang;
import com.example.datn_sd28_2025.entity.KhachHangGiamGia;
import com.example.datn_sd28_2025.repository.PhieuGiamGiaRepository;
import com.example.datn_sd28_2025.repository.KhachHangRepository;
import com.example.datn_sd28_2025.repository.KhachHangGiamGiaRepository;
import com.example.datn_sd28_2025.service.PhieuGiamGiaService;
import com.example.datn_sd28_2025.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PhieuGiamGiaServiceImpl implements PhieuGiamGiaService {

    @Autowired
    private PhieuGiamGiaRepository phieuGiamGiaRepository;
    
    @Autowired
    private KhachHangRepository khachHangRepository;
    
    @Autowired
    private KhachHangGiamGiaRepository khachHangGiamGiaRepository;
    
    @Autowired
    private EmailService emailService;

    @Override
    public List<PhieuGiamGiaDTO> getAll() {
        // Tự động vô hiệu hóa voucher hết hạn và hết số lượng trước khi trả về
        autoDisableExpiredVouchers();
        autoDisableOutOfStockVouchers();
        return phieuGiamGiaRepository.findAll().stream().map(this::convertToDto).toList();
    }
    
    private void autoDisableExpiredVouchers() {
        LocalDate now = LocalDate.now();
        List<PhieuGiamGia> expiredVouchers = phieuGiamGiaRepository.findAll().stream()
            .filter(v -> v.getTrangThai() == 1 && v.getNgayKetThuc().isBefore(now))
            .toList();
            
        for (PhieuGiamGia voucher : expiredVouchers) {
            voucher.setTrangThai(0);
            voucher.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            voucher.setNgayCapNhat(LocalDateTime.now());
            phieuGiamGiaRepository.save(voucher);
        }
    }
    
    private void autoDisableOutOfStockVouchers() {
        List<PhieuGiamGia> outOfStockVouchers = phieuGiamGiaRepository.findAll().stream()
            .filter(v -> v.getTrangThai() == 1 && v.getSoLuongDung() <= 0)
            .toList();
            
        for (PhieuGiamGia voucher : outOfStockVouchers) {
            voucher.setTrangThai(0);
            voucher.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            voucher.setNgayCapNhat(LocalDateTime.now());
            phieuGiamGiaRepository.save(voucher);
            System.out.println("Voucher " + voucher.getMaPhieuGiamGia() + " đã hết số lượng, tự động vô hiệu hóa");
        }
    }

    @Override
    public Optional<PhieuGiamGiaDTO> getById(Integer id) {
        return phieuGiamGiaRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public List<PhieuGiamGiaDTO> getActiveVouchers() {
        return phieuGiamGiaRepository.findValidVouchers(LocalDate.now()).stream().map(this::convertToDto).toList();
    }

    @Override
    public List<PhieuGiamGiaDTO> searchByQuery(String query) {
        return phieuGiamGiaRepository.findByNameContaining(query)
                .stream()
                .filter(voucher -> voucher.getTrangThai() == 1)
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Optional<PhieuGiamGiaDTO> getByCode(String code) {
        return phieuGiamGiaRepository.findByCode(code).map(this::convertToDto);
    }

    @Override
    @Transactional
    public PhieuGiamGiaDTO save(PhieuGiamGiaDTO phieuGiamGiaDTO) {
        PhieuGiamGia phieuGiamGia = convertToEntity(phieuGiamGiaDTO);
        
        // Tự động tạo mã phiếu giảm giá nếu chưa có
        if (phieuGiamGia.getMaPhieuGiamGia() == null || phieuGiamGia.getMaPhieuGiamGia().isEmpty()) {
            phieuGiamGia.setMaPhieuGiamGia(generateVoucherCode());
        }
        
        phieuGiamGia.setNgayTao(LocalDateTime.now());
        phieuGiamGia.setTrangThai(1); // Mặc định là hoạt động
        
        // Tự động set nguoiTao từ SecurityContext (nhân viên đang đăng nhập)
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            phieuGiamGia.setNguoiTao(currentUsername);
            phieuGiamGia.setNguoiCapNhat(currentUsername);
        }
        
        // Đặc biệt xử lý voucher riêng tư: số lượng = số khách hàng được chọn
        if (phieuGiamGiaDTO.getRiengTu() != null && phieuGiamGiaDTO.getRiengTu() && 
            phieuGiamGiaDTO.getSelectedCustomers() != null && !phieuGiamGiaDTO.getSelectedCustomers().isEmpty()) {
            // Voucher riêng tư: mỗi khách hàng có 1 phiếu riêng
            phieuGiamGia.setSoLuongDung(phieuGiamGiaDTO.getSelectedCustomers().size());
        }
        
        PhieuGiamGia savedVoucher = phieuGiamGiaRepository.save(phieuGiamGia);
        
        // Xử lý liên kết khách hàng cho voucher riêng tư
        if (phieuGiamGiaDTO.getSelectedCustomers() != null && !phieuGiamGiaDTO.getSelectedCustomers().isEmpty()) {
            saveCustomerVoucherRelations(savedVoucher, phieuGiamGiaDTO.getSelectedCustomers());
            
            // Gửi email thông báo voucher mới cho khách hàng
            sendVoucherNotificationToCustomers(savedVoucher, phieuGiamGiaDTO.getSelectedCustomers());
        }
        
        return convertToDto(savedVoucher);
    }

    @Override
    @Transactional
    public PhieuGiamGiaDTO update(Integer id, PhieuGiamGiaDTO phieuGiamGiaDTO) {
        System.out.println("=== DEBUG: Updating voucher with ID: " + id + " ===");
        System.out.println("Update data: " + phieuGiamGiaDTO.getTenPhieuGiamGia());
        System.out.println("Is private: " + phieuGiamGiaDTO.getRiengTu());
        System.out.println("Selected customers: " + (phieuGiamGiaDTO.getSelectedCustomers() != null ? phieuGiamGiaDTO.getSelectedCustomers().size() : "null"));
        
        return phieuGiamGiaRepository.findById(id).map(existingPhieuGiamGia -> {
            existingPhieuGiamGia.setTenPhieuGiamGia(phieuGiamGiaDTO.getTenPhieuGiamGia());
            existingPhieuGiamGia.setLoaiPhieuGiamGia(phieuGiamGiaDTO.getLoaiPhieuGiamGia());
            existingPhieuGiamGia.setGiaTriGiamGia(phieuGiamGiaDTO.getGiaTriGiamGia());
            existingPhieuGiamGia.setSoTienGiamToiDa(phieuGiamGiaDTO.getSoTienGiamToiDa());
            existingPhieuGiamGia.setHoaDonToiThieu(phieuGiamGiaDTO.getHoaDonToiThieu());
            existingPhieuGiamGia.setSoLuongDung(phieuGiamGiaDTO.getSoLuongDung());
            existingPhieuGiamGia.setNgayBatDau(phieuGiamGiaDTO.getNgayBatDau());
            existingPhieuGiamGia.setNgayKetThuc(phieuGiamGiaDTO.getNgayKetThuc());
            existingPhieuGiamGia.setRiengTu(phieuGiamGiaDTO.getRiengTu());
            existingPhieuGiamGia.setMoTa(phieuGiamGiaDTO.getMoTa());
            existingPhieuGiamGia.setTrangThai(phieuGiamGiaDTO.getTrangThai());
            existingPhieuGiamGia.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            existingPhieuGiamGia.setNgayCapNhat(LocalDateTime.now());
            
            // Đặc biệt xử lý voucher riêng tư: số lượng = số khách hàng được chọn
            if (phieuGiamGiaDTO.getRiengTu() != null && phieuGiamGiaDTO.getRiengTu() && 
                phieuGiamGiaDTO.getSelectedCustomers() != null && !phieuGiamGiaDTO.getSelectedCustomers().isEmpty()) {
                // Voucher riêng tư: mỗi khách hàng có 1 phiếu riêng
                existingPhieuGiamGia.setSoLuongDung(phieuGiamGiaDTO.getSelectedCustomers().size());
            }
            
            PhieuGiamGia savedVoucher = phieuGiamGiaRepository.save(existingPhieuGiamGia);
            
            // Cập nhật liên kết khách hàng cho voucher riêng tư
            if (phieuGiamGiaDTO.getRiengTu() != null && phieuGiamGiaDTO.getRiengTu()) {
                // Lấy danh sách khách hàng cũ trước khi cập nhật
                List<KhachHangGiamGia> oldRelations = khachHangGiamGiaRepository.findByPhieuGiamGiaId(savedVoucher.getId());
                List<Integer> oldCustomerIds = oldRelations.stream()
                    .map(rel -> rel.getKhachHang().getId())
                    .toList();
                
                // Cập nhật liên kết mới
                updateCustomerVoucherRelations(savedVoucher, phieuGiamGiaDTO.getSelectedCustomers());
                
                // Gửi email thông báo cho khách hàng mới và cũ
                handleVoucherUpdateEmails(savedVoucher, oldCustomerIds, phieuGiamGiaDTO.getSelectedCustomers());
            } else {
                // Nếu không còn là voucher riêng tư, xóa tất cả liên kết
                List<KhachHangGiamGia> oldRelations = khachHangGiamGiaRepository.findByPhieuGiamGiaId(savedVoucher.getId());
                List<Integer> oldCustomerIds = oldRelations.stream()
                    .map(rel -> rel.getKhachHang().getId())
                    .toList();
                
                // Gửi email thông báo voucher hết hiệu lực cho khách hàng cũ
                sendVoucherExpiredNotificationToCustomers(savedVoucher, oldCustomerIds);
                
                khachHangGiamGiaRepository.deleteByPhieuGiamGiaId(savedVoucher.getId());
            }
            
            return convertToDto(savedVoucher);
        }).orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        if (!phieuGiamGiaRepository.existsById(id)) {
            throw new RuntimeException("PhieuGiamGia not found with id " + id);
        }
        phieuGiamGiaRepository.deleteById(id);
    }

    @Override
    public PhieuGiamGiaDTO toggleStatus(Integer id) {
        return phieuGiamGiaRepository.findById(id).map(phieuGiamGia -> {
            LocalDate now = LocalDate.now();
            
            // Nếu voucher đã hết hạn, tự động set về trạng thái 0 (vô hiệu)
            if (phieuGiamGia.getNgayKetThuc().isBefore(now)) {
                if (phieuGiamGia.getTrangThai() == 1) {
                    // Tự động vô hiệu hóa voucher hết hạn
                    phieuGiamGia.setTrangThai(0);
                    phieuGiamGia.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
                    phieuGiamGia.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(phieuGiamGiaRepository.save(phieuGiamGia));
                } else {
                    // Không cho phép kích hoạt lại voucher đã hết hạn
                    throw new IllegalStateException("Không thể kích hoạt voucher đã hết thời gian hiệu lực");
                }
            }
            
            // Kiểm tra nếu voucher chưa bắt đầu và đang cố gắng kích hoạt
            if (phieuGiamGia.getTrangThai() == 0 && phieuGiamGia.getNgayBatDau().isAfter(now)) {
                throw new IllegalStateException("Voucher chưa đến thời gian hiệu lực");
            }
            
            // Toggle trạng thái bình thường
            phieuGiamGia.setTrangThai(phieuGiamGia.getTrangThai() == 1 ? 0 : 1);
            phieuGiamGia.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            phieuGiamGia.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(phieuGiamGiaRepository.save(phieuGiamGia));
        }).orElseThrow(() -> new RuntimeException("PhieuGiamGia not found with id " + id));
    }

    @Override
    public boolean isVoucherValid(String code, BigDecimal orderAmount) {
        Optional<PhieuGiamGia> voucher = phieuGiamGiaRepository.findByCode(code);
        
        if (voucher.isEmpty()) {
            return false;
        }
        
        PhieuGiamGia v = voucher.get();
        LocalDate now = LocalDate.now();
        
        return v.getTrangThai() == 1 &&
               v.getSoLuongDung() > 0 &&
               v.getNgayBatDau().compareTo(now) <= 0 &&
               v.getNgayKetThuc().compareTo(now) >= 0 &&
               (v.getHoaDonToiThieu() == null || orderAmount.compareTo(v.getHoaDonToiThieu()) >= 0);
    }

    @Override
    public List<PhieuGiamGiaDTO> getVouchersByCustomer(Integer customerId) {
        try {
            System.out.println("=== DEBUG: Getting vouchers for customer " + customerId + " ===");
            
            // Lấy tất cả voucher công khai (không riêng tư) và đang hoạt động
            List<PhieuGiamGia> allVouchers = phieuGiamGiaRepository.findAll();
            System.out.println("Total vouchers in database: " + allVouchers.size());
            
            List<PhieuGiamGia> publicVouchers = allVouchers.stream()
                .filter(v -> {
                    System.out.println("Checking voucher " + v.getMaPhieuGiamGia() + 
                        " - Status: " + v.getTrangThai() + 
                        " - Private: " + v.getRiengTu() + 
                        " - Quantity: " + v.getSoLuongDung() +
                        " - Start: " + v.getNgayBatDau() + 
                        " - End: " + v.getNgayKetThuc());
                    
                    return v.getTrangThai() == 1 && 
                           (v.getRiengTu() == null || !v.getRiengTu()) &&
                           v.getNgayBatDau() != null && v.getNgayKetThuc() != null &&
                           v.getSoLuongDung() > 0;
                           // Tạm thời bỏ check ngày để debug
                           // v.getNgayBatDau().compareTo(LocalDate.now()) <= 0 &&
                           // v.getNgayKetThuc().compareTo(LocalDate.now()) >= 0 &&
                })
                .toList();
            
            System.out.println("Public vouchers found: " + publicVouchers.size());

            // Lấy voucher riêng tư dành cho khách hàng này (chỉ những voucher chưa sử dụng)
            List<KhachHangGiamGia> customerVoucherRelations = khachHangGiamGiaRepository.findByKhachHangId(customerId);
            System.out.println("Customer voucher relations found: " + customerVoucherRelations.size());
            
            List<PhieuGiamGia> privateVouchers = customerVoucherRelations.stream()
                // Tạm thời bỏ filter daSuDung vì field chưa có trong database
                // .filter(relation -> relation.getDaSuDung() == null || !relation.getDaSuDung()) // Chỉ lấy voucher chưa sử dụng
                .map(KhachHangGiamGia::getPhieuGiamGia)
                .filter(v -> v != null && v.getTrangThai() == 1 &&
                            v.getNgayBatDau() != null && v.getNgayKetThuc() != null &&
                            v.getSoLuongDung() > 0)
                            // Tạm thời bỏ check ngày để debug
                            // v.getNgayBatDau().compareTo(LocalDate.now()) <= 0 &&
                            // v.getNgayKetThuc().compareTo(LocalDate.now()) >= 0 &&
                .toList();
            
            System.out.println("Private vouchers found: " + privateVouchers.size());

            // Kết hợp cả hai danh sách
            List<PhieuGiamGia> finalVouchers = new java.util.ArrayList<>(publicVouchers);
            finalVouchers.addAll(privateVouchers);
            
            System.out.println("Total vouchers to return: " + finalVouchers.size());

            return finalVouchers.stream()
                .distinct()
                .map(this::convertToDto)
                .toList();
        } catch (Exception e) {
            // Log error và trả về empty list thay vì throw exception
            System.err.println("Error getting vouchers for customer " + customerId + ": " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public List<PhieuGiamGiaDTO> getUsedVouchersByCustomer(Integer customerId) {
        try {
            // Lấy voucher riêng tư đã sử dụng bởi khách hàng này
            List<KhachHangGiamGia> usedVoucherRelations = khachHangGiamGiaRepository.findByKhachHangId(customerId);
            List<PhieuGiamGia> usedVouchers = usedVoucherRelations.stream()
                .filter(relation -> relation.getDaSuDung() != null && relation.getDaSuDung()) // Chỉ lấy voucher đã sử dụng
                .map(KhachHangGiamGia::getPhieuGiamGia)
                .filter(voucher -> voucher != null) // Đảm bảo voucher không null
                .toList();

            return usedVouchers.stream()
                .map(this::convertToDto)
                .toList();
        } catch (Exception e) {
            // Log error và trả về empty list thay vì throw exception
            System.err.println("Error getting used vouchers for customer " + customerId + ": " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

    @Override
    @Transactional
    public void decreaseUsage(Integer voucherId) {
        if (voucherId == null) return;
        try {
            phieuGiamGiaRepository.decrementUsage(voucherId);
        } catch (Exception e) {
            System.out.println("decreaseUsage voucher failed: " + e.getMessage());
        }
    }

    @Override
    public boolean canCustomerUseVoucher(Integer customerId, String voucherCode) {
        Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findByCode(voucherCode);
        
        if (voucherOpt.isEmpty()) {
            return false;
        }
        
        PhieuGiamGia voucher = voucherOpt.get();
        
        // Kiểm tra voucher có hợp lệ không
        LocalDate now = LocalDate.now();
        if (voucher.getTrangThai() != 1 || 
            voucher.getSoLuongDung() <= 0 ||
            voucher.getNgayBatDau().compareTo(now) > 0 ||
            voucher.getNgayKetThuc().compareTo(now) < 0) {
            return false;
        }
        
        // Nếu là voucher công khai (không riêng tư), ai cũng có thể dùng
        if (voucher.getRiengTu() == null || !voucher.getRiengTu()) {
            return true;
        }
        
            // Nếu là voucher riêng tư, kiểm tra khách hàng có được phân quyền không
            KhachHangGiamGia relation = khachHangGiamGiaRepository.findByPhieuGiamGiaIdAndKhachHangId(voucher.getId(), customerId);
            if (relation == null || relation.getTrangThai() != 1) {
                return false;
            }

            // Kiểm tra khách hàng đã sử dụng voucher này chưa (mỗi khách hàng chỉ được dùng voucher cá nhân 1 lần)
            // Tạm thời bỏ check daSuDung vì field chưa có trong database
            // Khi có field daSuDung, uncomment dòng dưới để đảm bảo mỗi khách hàng chỉ dùng 1 lần:
            // if (relation.getDaSuDung() != null && relation.getDaSuDung()) {
            //     return false; // Khách hàng đã sử dụng voucher này rồi, không thể dùng lại
            // }
        
        return true;
    }

    @Override
    public boolean markVoucherAsUsed(Integer customerId, String voucherCode, Double discountAmount) {
        Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findByCode(voucherCode);
        
        if (voucherOpt.isEmpty()) {
            return false;
        }
        
        PhieuGiamGia voucher = voucherOpt.get();
        
        // Kiểm tra voucher còn số lượng sử dụng không
        if (voucher.getSoLuongDung() <= 0) {
            return false; // Không còn số lượng để sử dụng
        }
        
        // Nếu là voucher công khai, chỉ cần giảm số lượng sử dụng
        if (voucher.getRiengTu() == null || !voucher.getRiengTu()) {
            // Giảm số lượng sử dụng
            voucher.setSoLuongDung(voucher.getSoLuongDung() - 1);
            
            // Nếu số lượng <= 0 sau khi giảm, vô hiệu hóa voucher
            if (voucher.getSoLuongDung() <= 0) {
                voucher.setTrangThai(0); // Vô hiệu hóa
                voucher.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
                voucher.setNgayCapNhat(LocalDateTime.now());
                System.out.println("Voucher " + voucherCode + " đã hết số lượng, tự động vô hiệu hóa");
            }
            
            phieuGiamGiaRepository.save(voucher);
            return true;
        }
        
        // Nếu là voucher riêng tư, kiểm tra khách hàng có quyền sử dụng không
        KhachHangGiamGia relation = khachHangGiamGiaRepository.findByPhieuGiamGiaIdAndKhachHangId(voucher.getId(), customerId);
        if (relation != null && relation.getTrangThai() == 1) {
            // Kiểm tra khách hàng đã sử dụng voucher này chưa (mỗi khách hàng chỉ được dùng 1 lần)
            // Tạm thời bỏ check daSuDung vì field chưa có trong database
            // if (relation.getDaSuDung() != null && relation.getDaSuDung()) {
            //     return false; // Khách hàng đã sử dụng voucher này rồi
            // }

            // Đánh dấu khách hàng đã sử dụng - tạm thời comment
            // relation.setDaSuDung(true);
            // relation.setNgaySuDung(LocalDateTime.now());
            // relation.setSoTienGiam(discountAmount);
            // khachHangGiamGiaRepository.save(relation);

            // Với voucher riêng tư: chỉ giảm số lượng, không vô hiệu hóa toàn bộ voucher
            // Vì mỗi khách hàng có 1 phiếu riêng, khi 1 người dùng thì chỉ phiếu của người đó hết
            voucher.setSoLuongDung(voucher.getSoLuongDung() - 1);
            
            // Chỉ vô hiệu hóa khi tất cả khách hàng đã sử dụng hết
            if (voucher.getSoLuongDung() <= 0) {
                voucher.setTrangThai(0); // Vô hiệu hóa
                voucher.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
                voucher.setNgayCapNhat(LocalDateTime.now());
                System.out.println("Voucher riêng tư " + voucherCode + " đã được sử dụng hết bởi tất cả khách hàng");
            }
            
            phieuGiamGiaRepository.save(voucher);
            return true;
        }
        
        return false;
    }

    @Override
    public boolean canWalkInCustomerUseVoucher(String voucherCode) {
        Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findByCode(voucherCode);
        
        if (voucherOpt.isEmpty()) {
            return false;
        }
        
        PhieuGiamGia voucher = voucherOpt.get();
        
        // Kiểm tra voucher có hợp lệ không
        LocalDate now = LocalDate.now();
        if (voucher.getTrangThai() != 1 || 
            voucher.getSoLuongDung() <= 0 ||
            voucher.getNgayBatDau().compareTo(now) > 0 ||
            voucher.getNgayKetThuc().compareTo(now) < 0) {
            return false;
        }
        
        // Chỉ cho phép voucher công khai (không riêng tư) cho khách vãng lai
        if (voucher.getRiengTu() != null && voucher.getRiengTu()) {
            return false; // Voucher riêng tư không thể dùng cho khách vãng lai
        }
        
        return true;
    }

    @Override
    public boolean markVoucherAsUsedForWalkIn(String voucherCode) {
        Optional<PhieuGiamGia> voucherOpt = phieuGiamGiaRepository.findByCode(voucherCode);
        
        if (voucherOpt.isEmpty()) {
            return false;
        }
        
        PhieuGiamGia voucher = voucherOpt.get();
        
        // Kiểm tra voucher còn số lượng sử dụng không
        if (voucher.getSoLuongDung() <= 0) {
            return false; // Không còn số lượng để sử dụng
        }
        
        // Kiểm tra voucher có phải là công khai không
        if (voucher.getRiengTu() != null && voucher.getRiengTu()) {
            return false; // Voucher riêng tư không thể dùng cho khách vãng lai
        }
        
        // Giảm số lượng sử dụng
        voucher.setSoLuongDung(voucher.getSoLuongDung() - 1);
        
        // Nếu số lượng <= 0 sau khi giảm, vô hiệu hóa voucher
        if (voucher.getSoLuongDung() <= 0) {
            voucher.setTrangThai(0); // Vô hiệu hóa
            voucher.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            voucher.setNgayCapNhat(LocalDateTime.now());
            System.out.println("Voucher " + voucherCode + " đã hết số lượng, tự động vô hiệu hóa");
        }
        
        phieuGiamGiaRepository.save(voucher);
        return true;
    }

    private String generateVoucherCode() {
        return "VOUCHER" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    private void saveCustomerVoucherRelations(PhieuGiamGia voucher, List<Integer> customerIds) {
        for (Integer customerId : customerIds) {
            Optional<KhachHang> khachHang = khachHangRepository.findById(customerId);
            if (khachHang.isPresent()) {
                KhachHangGiamGia relation = KhachHangGiamGia.builder()
                    .khachHang(khachHang.get())
                    .phieuGiamGia(voucher)
                    .nguoiSuDung(khachHang.get().getHoTen())
                    .ngayCap(LocalDateTime.now())
                    .trangThai(1)
                    .build();
                khachHangGiamGiaRepository.save(relation);
            }
        }
    }
    
    @Transactional
    public void updateCustomerVoucherRelations(PhieuGiamGia voucher, List<Integer> customerIds) {
        // Xóa các liên kết cũ
        khachHangGiamGiaRepository.deleteByPhieuGiamGiaId(voucher.getId());
        
        // Tạo liên kết mới
        if (customerIds != null && !customerIds.isEmpty()) {
            saveCustomerVoucherRelations(voucher, customerIds);
        }
    }

    private PhieuGiamGiaDTO convertToDto(PhieuGiamGia phieuGiamGia) {
        return PhieuGiamGiaDTO.builder()
                .id(phieuGiamGia.getId())
                .maPhieuGiamGia(phieuGiamGia.getMaPhieuGiamGia())
                .tenPhieuGiamGia(phieuGiamGia.getTenPhieuGiamGia())
                .loaiPhieuGiamGia(phieuGiamGia.getLoaiPhieuGiamGia())
                .giaTriGiamGia(phieuGiamGia.getGiaTriGiamGia())
                .soTienGiamToiDa(phieuGiamGia.getSoTienGiamToiDa())
                .hoaDonToiThieu(phieuGiamGia.getHoaDonToiThieu())
                .soLuongDung(phieuGiamGia.getSoLuongDung())
                .ngayBatDau(phieuGiamGia.getNgayBatDau())
                .ngayKetThuc(phieuGiamGia.getNgayKetThuc())
                .trangThai(phieuGiamGia.getTrangThai())
                .riengTu(phieuGiamGia.getRiengTu())
                .moTa(phieuGiamGia.getMoTa())
                .ngayTao(phieuGiamGia.getNgayTao())
                .ngayCapNhat(phieuGiamGia.getNgayCapNhat())
                .nguoiTao(phieuGiamGia.getNguoiTao())
                .nguoiCapNhat(phieuGiamGia.getNguoiCapNhat())
                .build();
    }

    private PhieuGiamGia convertToEntity(PhieuGiamGiaDTO dto) {
        return PhieuGiamGia.builder()
                .id(dto.getId())
                .maPhieuGiamGia(dto.getMaPhieuGiamGia())
                .tenPhieuGiamGia(dto.getTenPhieuGiamGia())
                .loaiPhieuGiamGia(dto.getLoaiPhieuGiamGia())
                .giaTriGiamGia(dto.getGiaTriGiamGia())
                .soTienGiamToiDa(dto.getSoTienGiamToiDa())
                .hoaDonToiThieu(dto.getHoaDonToiThieu())
                .soLuongDung(dto.getSoLuongDung())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .trangThai(dto.getTrangThai())
                .riengTu(dto.getRiengTu())
                .moTa(dto.getMoTa())
                .ngayTao(dto.getNgayTao())
                .ngayCapNhat(dto.getNgayCapNhat())
                .nguoiTao(dto.getNguoiTao())
                .nguoiCapNhat(dto.getNguoiCapNhat())
                .build();
    }
    
    /**
     * Gửi email thông báo voucher mới cho danh sách khách hàng
     */
    private void sendVoucherNotificationToCustomers(PhieuGiamGia voucher, List<Integer> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return;
        }
        
        for (Integer customerId : customerIds) {
            try {
                Optional<KhachHang> customerOpt = khachHangRepository.findById(customerId);
                if (customerOpt.isPresent()) {
                    KhachHang customer = customerOpt.get();
                    
                    // Chỉ gửi email nếu khách hàng có email
                    if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                        String discountAmount = formatDiscountAmount(voucher);
                        String validFrom = voucher.getNgayBatDau() != null ? voucher.getNgayBatDau().toString() : "N/A";
                        String validTo = voucher.getNgayKetThuc() != null ? voucher.getNgayKetThuc().toString() : "N/A";
                        
                        emailService.sendVoucherNotification(
                            customer.getEmail(),
                            customer.getHoTen(),
                            voucher.getMaPhieuGiamGia(),
                            voucher.getTenPhieuGiamGia(),
                            discountAmount,
                            validFrom,
                            validTo
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending voucher notification to customer " + customerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Gửi email thông báo voucher hết hiệu lực cho danh sách khách hàng
     */
    private void sendVoucherExpiredNotificationToCustomers(PhieuGiamGia voucher, List<Integer> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return;
        }
        
        for (Integer customerId : customerIds) {
            try {
                Optional<KhachHang> customerOpt = khachHangRepository.findById(customerId);
                if (customerOpt.isPresent()) {
                    KhachHang customer = customerOpt.get();
                    
                    // Chỉ gửi email nếu khách hàng có email
                    if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                        emailService.sendVoucherExpiredNotification(
                            customer.getEmail(),
                            customer.getHoTen(),
                            voucher.getMaPhieuGiamGia(),
                            voucher.getTenPhieuGiamGia()
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending voucher expired notification to customer " + customerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Xử lý gửi email khi cập nhật voucher assignment
     */
    private void handleVoucherUpdateEmails(PhieuGiamGia voucher, List<Integer> oldCustomerIds, List<Integer> newCustomerIds) {
        // Tạo final copies để sử dụng trong lambda expressions
        final List<Integer> finalOldCustomerIds = oldCustomerIds != null ? oldCustomerIds : List.of();
        final List<Integer> finalNewCustomerIds = newCustomerIds != null ? newCustomerIds : List.of();
        
        // Tìm khách hàng bị loại bỏ (có trong old nhưng không có trong new)
        List<Integer> removedCustomers = finalOldCustomerIds.stream()
            .filter(id -> !finalNewCustomerIds.contains(id))
            .toList();
        
        // Tìm khách hàng mới (có trong new nhưng không có trong old)
        List<Integer> addedCustomers = finalNewCustomerIds.stream()
            .filter(id -> !finalOldCustomerIds.contains(id))
            .toList();
        
        // Tìm khách hàng được giữ lại (có trong cả old và new)
        List<Integer> keptCustomers = finalOldCustomerIds.stream()
            .filter(finalNewCustomerIds::contains)
            .toList();
        
        // Gửi email thông báo hết hiệu lực cho khách hàng bị loại bỏ
        if (!removedCustomers.isEmpty()) {
            sendVoucherExpiredNotificationToCustomers(voucher, removedCustomers);
        }
        
        // Gửi email thông báo voucher mới cho khách hàng mới
        if (!addedCustomers.isEmpty()) {
            sendVoucherNotificationToCustomers(voucher, addedCustomers);
        }
        
        // Gửi email thông báo cập nhật cho khách hàng được giữ lại
        if (!keptCustomers.isEmpty()) {
            sendVoucherUpdatedNotificationToCustomers(voucher, keptCustomers);
        }
    }
    
    /**
     * Gửi email thông báo voucher đã được cập nhật cho danh sách khách hàng
     */
    private void sendVoucherUpdatedNotificationToCustomers(PhieuGiamGia voucher, List<Integer> customerIds) {
        if (customerIds == null || customerIds.isEmpty()) {
            return;
        }
        
        for (Integer customerId : customerIds) {
            try {
                Optional<KhachHang> customerOpt = khachHangRepository.findById(customerId);
                if (customerOpt.isPresent()) {
                    KhachHang customer = customerOpt.get();
                    
                    // Chỉ gửi email nếu khách hàng có email
                    if (customer.getEmail() != null && !customer.getEmail().trim().isEmpty()) {
                        String discountAmount = formatDiscountAmount(voucher);
                        String validFrom = voucher.getNgayBatDau() != null ? voucher.getNgayBatDau().toString() : "N/A";
                        String validTo = voucher.getNgayKetThuc() != null ? voucher.getNgayKetThuc().toString() : "N/A";
                        
                        emailService.sendVoucherUpdatedNotification(
                            customer.getEmail(),
                            customer.getHoTen(),
                            voucher.getMaPhieuGiamGia(),
                            voucher.getTenPhieuGiamGia(),
                            discountAmount,
                            validFrom,
                            validTo
                        );
                    }
                }
            } catch (Exception e) {
                System.err.println("Error sending voucher updated notification to customer " + customerId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Format discount amount for display
     */
    private String formatDiscountAmount(PhieuGiamGia voucher) {
        if ("PERCENT".equals(voucher.getLoaiPhieuGiamGia()) || 
            "1".equals(voucher.getLoaiPhieuGiamGia()) || 
            "Phần trăm".equals(voucher.getLoaiPhieuGiamGia())) {
            // Phần trăm
            return voucher.getGiaTriGiamGia() + "%";
        } else {
            // Số tiền cố định
            return String.format("%,.0f VNĐ", voucher.getGiaTriGiamGia().doubleValue());
        }
    }

    @Override
    public List<PhieuGiamGiaDTO> getAvailableVouchers(Integer customerId) {
        try {
            System.out.println("=== DEBUG: getAvailableVouchers called with customerId: " + customerId + " ===");
            
            // Lấy tất cả voucher công khai (không riêng tư) và đang hoạt động
            List<PhieuGiamGia> allVouchers = phieuGiamGiaRepository.findAll();
            System.out.println("Total vouchers in database: " + allVouchers.size());
            
            List<PhieuGiamGia> publicVouchers = allVouchers.stream()
                .filter(v -> {
                    // Log all vouchers for debugging
                    System.out.println("Checking voucher: " + v.getMaPhieuGiamGia() + 
                        " - Status: " + v.getTrangThai() + 
                        " - Private: " + v.getRiengTu() + 
                        " - Start: " + v.getNgayBatDau() + 
                        " - End: " + v.getNgayKetThuc() + 
                        " - Quantity: " + v.getSoLuongDung());
                    
                    // Tạm thời bỏ filter status = 1 để test (có thể bật lại sau)
                    // Chỉ filter: không riêng tư, có ngày, có số lượng
                    boolean isValid = (v.getTrangThai() == 1 || v.getTrangThai() == 0) && // Cho phép cả status 0 và 1 để test
                           (v.getRiengTu() == null || !v.getRiengTu()) &&
                           v.getNgayBatDau() != null && v.getNgayKetThuc() != null &&
                           v.getSoLuongDung() != null && v.getSoLuongDung() > 0;
                    
                    if (!isValid && (v.getRiengTu() == null || !v.getRiengTu())) {
                        System.out.println("❌ Voucher filtered out: " + v.getMaPhieuGiamGia() + 
                            " - Status check: " + (v.getTrangThai() == 1) +
                            " - Private check: " + (v.getRiengTu() == null || !v.getRiengTu()) +
                            " - Start check: " + (v.getNgayBatDau() != null) +
                            " - End check: " + (v.getNgayKetThuc() != null) +
                            " - Quantity check: " + (v.getSoLuongDung() != null && v.getSoLuongDung() > 0));
                    } else if (isValid) {
                        System.out.println("✅ Voucher passed: " + v.getMaPhieuGiamGia());
                    }
                    return isValid;
                })
                .toList();
            
            System.out.println("Public vouchers found: " + publicVouchers.size());
            
            List<PhieuGiamGia> finalVouchers = new java.util.ArrayList<>(publicVouchers);
            
            // Nếu có customerId, thêm các voucher riêng tư của customer đó
            if (customerId != null) {
                List<KhachHangGiamGia> customerVoucherRelations = khachHangGiamGiaRepository.findByKhachHangId(customerId);
                System.out.println("Customer voucher relations found: " + customerVoucherRelations.size());
                
                List<PhieuGiamGia> privateVouchers = customerVoucherRelations.stream()
                    .map(KhachHangGiamGia::getPhieuGiamGia)
                    .filter(v -> v != null && v.getTrangThai() == 1 &&
                                v.getNgayBatDau() != null && v.getNgayKetThuc() != null &&
                                v.getSoLuongDung() != null && v.getSoLuongDung() > 0)
                    .toList();
                
                System.out.println("Private vouchers found: " + privateVouchers.size());
                finalVouchers.addAll(privateVouchers);
            }
            
            System.out.println("Total vouchers to return: " + finalVouchers.size());
            
            // Loại bỏ trùng lặp và convert sang DTO
            List<PhieuGiamGiaDTO> result = finalVouchers.stream()
                .distinct()
                .map(this::convertToDto)
                .toList();
            
            System.out.println("Final DTOs count: " + result.size());
            return result;
        } catch (Exception e) {
            System.err.println("Error getting available vouchers: " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
}
