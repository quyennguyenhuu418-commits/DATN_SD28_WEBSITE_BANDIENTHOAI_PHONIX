package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.DotGiamGiaSanPhamDTO;
import com.example.datn_sd28_2025.dto.KhuyenMaiDTO;
import com.example.datn_sd28_2025.entity.DotGiamGiaSanPham;
import com.example.datn_sd28_2025.entity.KhuyenMai;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.repository.DotGiamGiaSanPhamRepository;
import com.example.datn_sd28_2025.repository.KhuyenMaiRepository;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
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
        List<DotGiamGiaSanPham> list = dotGiamGiaSanPhamRepository.findByKhuyenMaiIdWithDetails(idKhuyenMai);
        return list.stream().map(this::convertDotGiamGiaToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public DotGiamGiaSanPhamDTO addProductToPromotion(Integer idKhuyenMai, Integer idSanPham, String nguoiTao) {
        // Kiểm tra xem đợt giảm giá có tồn tại không
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(idKhuyenMai)
                .orElseThrow(() -> new RuntimeException("Đợt giảm giá không tồn tại với ID: " + idKhuyenMai));

        // Kiểm tra xem sản phẩm có tồn tại không
        SanPham sanPham = sanPhamRepository.findById(idSanPham)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại với ID: " + idSanPham));

        // Kiểm tra xem sản phẩm đã được thêm vào đợt giảm giá chưa
        Optional<DotGiamGiaSanPham> existing = dotGiamGiaSanPhamRepository
                .findByKhuyenMaiIdAndSanPhamId(idKhuyenMai, idSanPham);

        if (existing.isPresent()) {
            throw new RuntimeException("Sản phẩm đã được thêm vào đợt giảm giá này");
        }

        // Tạo mới
        DotGiamGiaSanPham dotGiamGia = DotGiamGiaSanPham.builder()
                .khuyenMai(khuyenMai)
                .sanPham(sanPham)
                .phanTramGiam(null) // Mặc định dùng phần trăm của đợt giảm giá
                .ngayTao(LocalDateTime.now())
                .nguoiTao(nguoiTao)
                .build();

        DotGiamGiaSanPham saved = dotGiamGiaSanPhamRepository.save(dotGiamGia);
        return convertDotGiamGiaToDto(saved);
    }

    @Override
    @Transactional
    public void removeProductFromPromotion(Integer idKhuyenMai, Integer idSanPham) {
        Optional<DotGiamGiaSanPham> existing = dotGiamGiaSanPhamRepository
                .findByKhuyenMaiIdAndSanPhamId(idKhuyenMai, idSanPham);

        if (existing.isEmpty()) {
            throw new RuntimeException("Sản phẩm không có trong đợt giảm giá này");
        }

        dotGiamGiaSanPhamRepository.deleteByKhuyenMaiIdAndSanPhamId(idKhuyenMai, idSanPham);
    }

    @Override
    @Transactional
    public void addMultipleProducts(Integer idKhuyenMai, List<Integer> idSanPhams, String nguoiTao) {
        // Kiểm tra đợt giảm giá
        KhuyenMai khuyenMai = khuyenMaiRepository.findById(idKhuyenMai)
                .orElseThrow(() -> new RuntimeException("Đợt giảm giá không tồn tại với ID: " + idKhuyenMai));

        List<DotGiamGiaSanPham> list = new ArrayList<>();

        for (Integer idSanPham : idSanPhams) {
            // Kiểm tra sản phẩm tồn tại
            SanPham sanPham = sanPhamRepository.findById(idSanPham).orElse(null);
            if (sanPham == null) {
                continue; // Bỏ qua sản phẩm không tồn tại
            }

            // Kiểm tra đã tồn tại chưa
            Optional<DotGiamGiaSanPham> existing = dotGiamGiaSanPhamRepository
                    .findByKhuyenMaiIdAndSanPhamId(idKhuyenMai, idSanPham);

            if (existing.isPresent()) {
                continue; // Bỏ qua sản phẩm đã tồn tại
            }

            // Tạo mới
            DotGiamGiaSanPham dotGiamGia = DotGiamGiaSanPham.builder()
                    .khuyenMai(khuyenMai)
                    .sanPham(sanPham)
                    .phanTramGiam(null)
                    .ngayTao(LocalDateTime.now())
                    .nguoiTao(nguoiTao)
                    .build();

            list.add(dotGiamGia);
        }

        if (!list.isEmpty()) {
            dotGiamGiaSanPhamRepository.saveAll(list);
        }
    }

    @Override
    public long countAppliedProducts(Integer idKhuyenMai) {
        return dotGiamGiaSanPhamRepository.countByKhuyenMaiId(idKhuyenMai);
    }

    private DotGiamGiaSanPhamDTO convertDotGiamGiaToDto(DotGiamGiaSanPham entity) {
        return DotGiamGiaSanPhamDTO.builder()
                .id(entity.getId())
                .idKhuyenMai(entity.getKhuyenMai().getId())
                .idSanPham(entity.getSanPham().getId())
                .tenSanPham(entity.getSanPham().getTenSanPham())
                .maSanPham(entity.getSanPham().getMaSanPham())
                .tenHang(entity.getSanPham().getHang() != null ? entity.getSanPham().getHang().getTen() : null)
                .tenDanhMuc(entity.getSanPham().getDanhMuc() != null ? entity.getSanPham().getDanhMuc().getTenDanhMuc() : null)
                .phanTramGiam(entity.getPhanTramGiam())
                .ngayTao(entity.getNgayTao())
                .nguoiTao(entity.getNguoiTao())
                .build();
    }
}

