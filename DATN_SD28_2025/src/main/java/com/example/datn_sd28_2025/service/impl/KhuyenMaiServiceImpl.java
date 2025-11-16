package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.DotGiamGiaSanPhamDTO;
import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.entity.DotGiamGiaSanPham;
import com.example.datn_sd28_2025.entity.KhuyenMai;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.repository.DotGiamGiaSanPhamRepository;
import com.example.datn_sd28_2025.repository.KhuyenMaiRepository;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.service.KhuyenMaiService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Autowired
    private DotGiamGiaSanPhamRepository dotGiamGiaSanPhamRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Override
    public List<KhuyenMaiDTO> getAll() {
        return khuyenMaiRepository.findAll().stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<KhuyenMaiDTO> getById(Integer id) {
        return khuyenMaiRepository.findById(id).map(this::convertToDto);
    }

    @Override
    public List<KhuyenMaiDTO> getActivePromotions() {
        return khuyenMaiRepository.findActivePromotions(LocalDateTime.now()).stream().map(this::convertToDto).toList();
    }

    @Override
    public Optional<KhuyenMaiDTO> getByCode(String code) {
        KhuyenMai khuyenMai = khuyenMaiRepository.findByMaKhuyenMai(code);
        return khuyenMai != null ? Optional.of(convertToDto(khuyenMai)) : Optional.empty();
    }

    @Override
    public KhuyenMaiDTO save(KhuyenMaiDTO khuyenMaiDTO) {
        KhuyenMai khuyenMai = convertToEntity(khuyenMaiDTO);

        // Tự động tạo mã khuyến mãi nếu chưa có
        if (khuyenMai.getMaKhuyenMai() == null || khuyenMai.getMaKhuyenMai().isEmpty()) {
            khuyenMai.setMaKhuyenMai(generatePromotionCode());
        }

        khuyenMai.setNgayTao(LocalDateTime.now());
        khuyenMai.setTrangThai(1); // Mặc định là hoạt động

        // Tự động set nguoiTao từ SecurityContext (nhân viên đang đăng nhập)
        String currentUsername = com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername();
        if (currentUsername != null && !currentUsername.trim().isEmpty() && !"System".equals(currentUsername)) {
            khuyenMai.setNguoiTao(currentUsername);
            khuyenMai.setNguoiCapNhat(currentUsername);
        }

        return convertToDto(khuyenMaiRepository.save(khuyenMai));
    }

    @Override
    public KhuyenMaiDTO update(Integer id, KhuyenMaiDTO khuyenMaiDTO) {
        return khuyenMaiRepository.findById(id).map(existingKhuyenMai -> {
            existingKhuyenMai.setTenKhuyenMai(khuyenMaiDTO.getTenKhuyenMai());
            existingKhuyenMai.setMoTa(khuyenMaiDTO.getMoTa());
            existingKhuyenMai.setPhanTramGiam(khuyenMaiDTO.getPhanTramGiam());
            existingKhuyenMai.setGiamToiDa(khuyenMaiDTO.getGiamToiDa());
            existingKhuyenMai.setNgayBatDau(khuyenMaiDTO.getNgayBatDau());
            existingKhuyenMai.setNgayKetThuc(khuyenMaiDTO.getNgayKetThuc());
            existingKhuyenMai.setTrangThai(khuyenMaiDTO.getTrangThai());
            existingKhuyenMai.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            existingKhuyenMai.setNgayCapNhat(LocalDateTime.now());

            return convertToDto(khuyenMaiRepository.save(existingKhuyenMai));
        }).orElseThrow(() -> new RuntimeException("KhuyenMai not found with id " + id));
    }

    @Override
    public void delete(Integer id) {
        if (!khuyenMaiRepository.existsById(id)) {
            throw new RuntimeException("KhuyenMai not found with id " + id);
        }
        khuyenMaiRepository.deleteById(id);
    }

    @Override
    public KhuyenMaiDTO toggleStatus(Integer id) {
        return khuyenMaiRepository.findById(id).map(khuyenMai -> {
            khuyenMai.setTrangThai(khuyenMai.getTrangThai() == 1 ? 0 : 1);
            khuyenMai.setNguoiCapNhat(com.example.datn_sd28_2025.util.SecurityUtil.getCurrentUsername());
            khuyenMai.setNgayCapNhat(LocalDateTime.now());
            return convertToDto(khuyenMaiRepository.save(khuyenMai));
        }).orElseThrow(() -> new RuntimeException("KhuyenMai not found with id " + id));
    }

    private String generatePromotionCode() {
        return "PROMO" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private KhuyenMaiDTO convertToDto(KhuyenMai khuyenMai) {
        return KhuyenMaiDTO.builder()
                .id(khuyenMai.getId())
                .maKhuyenMai(khuyenMai.getMaKhuyenMai())
                .tenKhuyenMai(khuyenMai.getTenKhuyenMai())
                .moTa(khuyenMai.getMoTa())
                .phanTramGiam(khuyenMai.getPhanTramGiam())
                .giamToiDa(khuyenMai.getGiamToiDa())
                .ngayBatDau(khuyenMai.getNgayBatDau())
                .ngayKetThuc(khuyenMai.getNgayKetThuc())
                .ngayTao(khuyenMai.getNgayTao())
                .ngayCapNhat(khuyenMai.getNgayCapNhat())
                .nguoiTao(khuyenMai.getNguoiTao())
                .nguoiCapNhat(khuyenMai.getNguoiCapNhat())
                .trangThai(khuyenMai.getTrangThai())
                .build();
    }

    private KhuyenMai convertToEntity(KhuyenMaiDTO dto) {
        return KhuyenMai.builder()
                .id(dto.getId())
                .maKhuyenMai(dto.getMaKhuyenMai())
                .tenKhuyenMai(dto.getTenKhuyenMai())
                .moTa(dto.getMoTa())
                .phanTramGiam(dto.getPhanTramGiam())
                .giamToiDa(dto.getGiamToiDa())
                .ngayBatDau(dto.getNgayBatDau())
                .ngayKetThuc(dto.getNgayKetThuc())
                .ngayTao(dto.getNgayTao())
                .ngayCapNhat(dto.getNgayCapNhat())
                .nguoiTao(dto.getNguoiTao())
                .nguoiCapNhat(dto.getNguoiCapNhat())
                .trangThai(dto.getTrangThai())
                .build();
    }

    // ============ QUẢN LÝ SẢN PHẨM ÁP DỤNG ĐỢT GIẢM GIÁ ============

    @Override
    public List<DotGiamGiaSanPhamDTO> getAppliedProducts(Integer idKhuyenMai) {
        System.out.println("=== getAppliedProducts Debug ===");
        System.out.println("KhuyenMai ID: " + idKhuyenMai);
        
        List<DotGiamGiaSanPham> list = dotGiamGiaSanPhamRepository.findByKhuyenMaiIdWithDetails(idKhuyenMai);
        System.out.println("Found " + list.size() + " DotGiamGiaSanPham records");
        
        for (DotGiamGiaSanPham item : list) {
            System.out.println("Item ID: " + item.getId());
            System.out.println("ChiTietSanPham ID: " + (item.getChiTietSanPham() != null ? item.getChiTietSanPham().getId() : "NULL"));
            if (item.getChiTietSanPham() != null && item.getChiTietSanPham().getSanPham() != null) {
                System.out.println("SanPham ID: " + item.getChiTietSanPham().getSanPham().getId());
                System.out.println("SanPham Name: " + item.getChiTietSanPham().getSanPham().getTenSanPham());
            }
        }
        
        List<DotGiamGiaSanPhamDTO> result = list.stream().map(this::convertDotGiamGiaToDto).collect(Collectors.toList());
        System.out.println("Converted to " + result.size() + " DTOs");
        System.out.println("=== End getAppliedProducts Debug ===");
        
        return result;
    }

    @Override
    @Transactional
    public DotGiamGiaSanPhamDTO addProductToPromotion(Integer idKhuyenMai, Integer idChiTietSanPham, String nguoiTao) {
        // Kiểm tra xem đợt giảm giá có tồn tại không
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(idKhuyenMai)
                .orElseThrow(() -> new RuntimeException("Đợt giảm giá không tồn tại với ID: " + idKhuyenMai));

        // Kiểm tra xem chi tiết sản phẩm có tồn tại không
        ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại với ID: " + idChiTietSanPham));

        // Kiểm tra xem chi tiết sản phẩm đã được thêm vào đợt giảm giá chưa
        Optional<DotGiamGiaSanPham> existing = dotGiamGiaSanPhamRepository
                .findByKhuyenMaiIdAndChiTietSanPhamId(idKhuyenMai, idChiTietSanPham);

        if (existing.isPresent()) {
            throw new RuntimeException("Chi tiết sản phẩm đã được thêm vào đợt giảm giá này");
        }

        // Tạo mới
        DotGiamGiaSanPham dotGiamGia = DotGiamGiaSanPham.builder()
                .khuyenMai(khuyenMai)
                .chiTietSanPham(chiTietSanPham)
                .phanTramGiam(khuyenMai.getPhanTramGiam()) // Luôn fill giá trị phần trăm giảm của đợt
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nguoiTao)
                .build();

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGia);
        return convertDotGiamGiaToDto(saved);
    }

    @Override
    @Transactional
    public void removeProductFromPromotion(Integer idKhuyenMai, Integer idChiTietSanPham) {
        Optional<DotGiamGiaSanPham> existing = dotGiamGiaSanPhamRepository
                .findByKhuyenMaiIdAndChiTietSanPhamId(idKhuyenMai, idChiTietSanPham);

        if (existing.isEmpty()) {
            throw new RuntimeException("Chi tiết sản phẩm không có trong đợt giảm giá này");
        }

        dotGiamGiaSanPhamRepository.deleteByKhuyenMaiIdAndChiTietSanPhamId(idKhuyenMai, idChiTietSanPham);
    }

    @Override
    @Transactional
    public void addMultipleProductsWithQuantity(Integer idKhuyenMai, 
            List<com.example.datn_sd28_2025.controller.KhuyenMaiController.ProductQuantityData> products, 
            String nguoiTao) {
        System.out.println("=== addMultipleProductsWithQuantity Debug ===");
        System.out.println("KhuyenMai ID: " + idKhuyenMai);
        System.out.println("Products count: " + (products != null ? products.size() : 0));
        System.out.println("NguoiTao: " + nguoiTao);
        
        // Kiểm tra đợt giảm giá
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(idKhuyenMai)
                .orElseThrow(() -> new RuntimeException("Đợt giảm giá không tồn tại với ID: " + idKhuyenMai));

        // Chuẩn hóa danh sách, loại trùng và giá trị không hợp lệ
        List<com.example.datn_sd28_2025.controller.KhuyenMaiController.ProductQuantityData> normalizedProducts = 
                products == null ? new ArrayList<>() :
                products.stream()
                        .filter(p -> p != null && p.getIdChiTietSanPham() != null && p.getIdChiTietSanPham() > 0)
                        .distinct()
                        .toList();
        System.out.println("Normalized products: " + normalizedProducts.size());

        // Xóa toàn bộ mapping cũ trước, sau đó ghi lại toàn bộ danh sách được chọn
        System.out.println("Deleting existing products for KhuyenMai " + idKhuyenMai);
        dotGiamGiaSanPhamRepository.deleteByKhuyenMaiId(idKhuyenMai);
        // Bắt buộc flush để đảm bảo xóa được thực thi trước khi insert, tránh vi phạm unique
        try {
            dotGiamGiaSanPhamRepository.flush();
        } catch (Exception e) {
            System.out.println("Flush after delete failed (safe to ignore if already synced): " + e.getMessage());
        }
        System.out.println("Deleted existing products");

        if (normalizedProducts.isEmpty()) {
            System.out.println("No products selected to save");
            return;
        }

        List<DotGiamGiaSanPham> list = new ArrayList<>();
        for (com.example.datn_sd28_2025.controller.KhuyenMaiController.ProductQuantityData productData : normalizedProducts) {
            Integer idChiTietSanPham = productData.getIdChiTietSanPham();
            Integer soLuongToiDa = productData.getSoLuongToiDa();
            System.out.println("Creating mapping for ChiTietSanPham ID: " + idChiTietSanPham + ", soLuongToiDa: " + soLuongToiDa);
            
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                    .orElse(null);
            if (chiTietSanPham == null) {
                System.out.println("ChiTietSanPham " + idChiTietSanPham + " not found, skipping");
                continue;
            }
            DotGiamGiaSanPham dotGiamGia = DotGiamGiaSanPham.builder()
                    .khuyenMai(khuyenMai)
                    .chiTietSanPham(chiTietSanPham)
                    .phanTramGiam(khuyenMai.getPhanTramGiam())
                    .soLuongToiDa(soLuongToiDa) // Set số lượng tối đa
                    .soLuongDaBan(0) // Khởi tạo số lượng đã bán = 0
                    .ngayTao(LocalDateTime.now())
                    .nguoiTao(nguoiTao)
                    .build();

            list.add(dotGiamGia);
        }

        if (!list.isEmpty()) {
            System.out.println("Saving " + list.size() + " DotGiamGiaSanPham records");
            dotGiamGiaSanPhamRepository.saveAll(list);
            dotGiamGiaSanPhamRepository.flush();
            System.out.println("Saved successfully");
        } else {
            System.out.println("No valid products to save");
        }
    }

    @Override
    @Transactional
    public void addMultipleProducts(Integer idKhuyenMai, List<Integer> idChiTietSanPhams, String nguoiTao) {
        System.out.println("=== addMultipleProducts Debug ===");
        System.out.println("KhuyenMai ID: " + idKhuyenMai);
        System.out.println("ID ChiTietSanPhams (raw): " + idChiTietSanPhams);
        System.out.println("NguoiTao: " + nguoiTao);
        
        // Kiểm tra đợt giảm giá
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(idKhuyenMai)
                .orElseThrow(() -> new RuntimeException("Đợt giảm giá không tồn tại với ID: " + idKhuyenMai));

        // Chuẩn hóa danh sách ID, loại trùng và giá trị không hợp lệ
        List<Integer> normalizedIds = idChiTietSanPhams == null ? new ArrayList<>() :
                idChiTietSanPhams.stream()
                        .filter(id -> id != null && id > 0)
                        .distinct()
                        .toList();
        System.out.println("Normalized CTSP IDs: " + normalizedIds);

        // Xóa toàn bộ mapping cũ trước, sau đó ghi lại toàn bộ danh sách được chọn
        System.out.println("Deleting existing products for KhuyenMai " + idKhuyenMai);
        dotGiamGiaSanPhamRepository.deleteByKhuyenMaiId(idKhuyenMai);
        // Bắt buộc flush để đảm bảo xóa được thực thi trước khi insert, tránh vi phạm unique
        try {
            dotGiamGiaSanPhamRepository.flush();
        } catch (Exception e) {
            System.out.println("Flush after delete failed (safe to ignore if already synced): " + e.getMessage());
        }
        System.out.println("Deleted existing products");

        if (normalizedIds.isEmpty()) {
            System.out.println("No products selected to save");
            return;
        }

        List<DotGiamGiaSanPham> list = new ArrayList<>();
        for (Integer idChiTietSanPham : normalizedIds) {
            System.out.println("Creating mapping for ChiTietSanPham ID: " + idChiTietSanPham);
            ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(idChiTietSanPham)
                    .orElse(null);
            if (chiTietSanPham == null) {
                System.out.println("ChiTietSanPham " + idChiTietSanPham + " not found, skipping");
                continue;
            }
            DotGiamGiaSanPham dotGiamGia = DotGiamGiaSanPham.builder()
                    .khuyenMai(khuyenMai)
                    .chiTietSanPham(chiTietSanPham)
                    .phanTramGiam(khuyenMai.getPhanTramGiam())
                    .soLuongToiDa(null) // Không giới hạn số lượng cho format cũ
                    .soLuongDaBan(0) // Khởi tạo số lượng đã bán = 0
                    .ngayTao(LocalDateTime.now())
                    .nguoiTao(nguoiTao)
                    .build();
            list.add(dotGiamGia);
        }
        
        if (!list.isEmpty()) {
            System.out.println("Saving " + list.size() + " products");
            dotGiamGiaSanPhamRepository.saveAll(list);
            System.out.println("Saved successfully");
        } else {
            System.out.println("No valid products to save after normalization");
        }
        System.out.println("=== End addMultipleProducts Debug ===");
    }

    @Override
    public long countAppliedProducts(Integer idKhuyenMai) {
        return dotGiamGiaSanPhamRepository.countByKhuyenMaiId(idKhuyenMai);
    }

    private DotGiamGiaSanPhamDTO convertDotGiamGiaToDto(DotGiamGiaSanPham entity) {
        System.out.println("=== convertDotGiamGiaToDto Debug ===");
        System.out.println("Entity ID: " + entity.getId());
        System.out.println("ChiTietSanPham: " + (entity.getChiTietSanPham() != null ? "NOT NULL" : "NULL"));
        
        if (entity.getChiTietSanPham() != null) {
            System.out.println("ChiTietSanPham ID: " + entity.getChiTietSanPham().getId());
            System.out.println("SanPham: " + (entity.getChiTietSanPham().getSanPham() != null ? "NOT NULL" : "NULL"));
            
            if (entity.getChiTietSanPham().getSanPham() != null) {
                System.out.println("SanPham ID: " + entity.getChiTietSanPham().getSanPham().getId());
                System.out.println("SanPham Name: " + entity.getChiTietSanPham().getSanPham().getTenSanPham());
                System.out.println("Hang: " + (entity.getChiTietSanPham().getSanPham().getHang() != null ? "NOT NULL" : "NULL"));
                System.out.println("DanhMuc: " + (entity.getChiTietSanPham().getSanPham().getDanhMuc() != null ? "NOT NULL" : "NULL"));
            }
        }
        
        DotGiamGiaSanPhamDTO dto = DotGiamGiaSanPhamDTO.builder()
                .id(entity.getId())
                .idKhuyenMai(entity.getKhuyenMai().getId())
                .idChiTietSanPham(entity.getChiTietSanPham().getId())
                .tenSanPham(entity.getChiTietSanPham().getSanPham().getTenSanPham())
                .maSanPham(entity.getChiTietSanPham().getSanPham().getMaSanPham())
                .tenHang(entity.getChiTietSanPham().getSanPham().getHang() != null ? entity.getChiTietSanPham().getSanPham().getHang().getTen() : null)
                .tenDanhMuc(entity.getChiTietSanPham().getSanPham().getDanhMuc() != null ? entity.getChiTietSanPham().getSanPham().getDanhMuc().getTenDanhMuc() : null)
                .phanTramGiam(entity.getPhanTramGiam())
                .soLuongToiDa(entity.getSoLuongToiDa())
                .soLuongDaBan(entity.getSoLuongDaBan())
                .ngayTao(entity.getNgayTao())
                .nguoiTao(entity.getNguoiTao())
                .build();
        
        System.out.println("DTO created: " + dto.getTenSanPham() + " - " + dto.getTenDanhMuc());
        System.out.println("=== End convertDotGiamGiaToDto Debug ===");
        
        return dto;
    }
}

