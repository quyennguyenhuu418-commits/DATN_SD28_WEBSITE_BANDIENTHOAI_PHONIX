package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.HoaDonDTO;
import com.example.datn_sd28_2025.dto.HoaDonTrackingDTO;
import com.example.datn_sd28_2025.dto.HoaDonSearchRequestDTO;
import com.example.datn_sd28_2025.dto.PosOrderRequest;
import com.example.datn_sd28_2025.dto.ChiTietHoaDonDTO;
import com.example.datn_sd28_2025.entity.HoaDon;
import com.example.datn_sd28_2025.entity.HoaDonCt;
import com.example.datn_sd28_2025.entity.ChiTietSanPham;
import com.example.datn_sd28_2025.entity.SanPham;
import com.example.datn_sd28_2025.entity.ImeiDaBan;
import com.example.datn_sd28_2025.entity.Ram;
import com.example.datn_sd28_2025.entity.Rom;
import com.example.datn_sd28_2025.entity.MauSac;
import com.example.datn_sd28_2025.repository.HoaDonRepository;
import com.example.datn_sd28_2025.repository.HoaDonCtRepository;
import com.example.datn_sd28_2025.repository.ChiTietSanPhamRepository;
import com.example.datn_sd28_2025.service.HoaDonService;
import com.example.datn_sd28_2025.service.ImeiDaBanService;
import com.example.datn_sd28_2025.util.OrderStatusUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class HoaDonServiceImpl implements HoaDonService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonCtRepository hoaDonCtRepository;

    @Autowired
    private ChiTietSanPhamRepository chiTietSanPhamRepository;

    @Autowired
    private ImeiDaBanService imeiDaBanService;

    @Override
    public List<HoaDonDTO> getAll() {
        return hoaDonRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public HoaDonDTO getById(Integer id) {
        return hoaDonRepository.findById(id)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public HoaDonDTO getByMaHoaDon(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .map(this::convertToDto)
                .orElse(null);
    }

    @Override
    public List<HoaDonDTO> getByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangId(khachHangId)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByTrangThai(Integer trangThai) {
        return hoaDonRepository.findByTrangThai(trangThai)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByLoaiHoaDon(String loaiHoaDon) {
        return hoaDonRepository.findByLoaiHoaDon(loaiHoaDon)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public List<HoaDonDTO> getByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.findByNgayTaoBetween(startDate, endDate)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public Page<HoaDonDTO> searchHoaDon(String keyword, Pageable pageable) {
        return hoaDonRepository.searchHoaDon(keyword, pageable)
                .map(this::convertToDto);
    }

    @Override
    @Transactional
    public HoaDonDTO createOrder(PosOrderRequest request) {
        // Create the main invoice
        HoaDon hoaDon = new HoaDon();
        hoaDon.setKhachHangId(request.getHoaDon().getKhachHangId());
        hoaDon.setNhanVienId(request.getHoaDon().getNhanVienId());
        hoaDon.setPhieuGiamGiaId(request.getHoaDon().getPhieuGiamGiaId());
        hoaDon.setTongTien(request.getHoaDon().getTongTien());
        hoaDon.setTenKhachHang(request.getHoaDon().getTenKhachHang());
        hoaDon.setSoDienThoai(request.getHoaDon().getSoDienThoai());
        hoaDon.setDiaChi(request.getHoaDon().getDiaChi());
        hoaDon.setTongTienSauGiam(request.getHoaDon().getTongTienSauGiam());
        hoaDon.setNgayThanhToan(request.getHoaDon().getNgayThanhToan());
        hoaDon.setLoaiHoaDon(request.getHoaDon().getLoaiHoaDon());

        // Set status based on order type
        hoaDon.setTrangThai(OrderStatusUtil.getInitialStatus(
                request.getHoaDon().getLoaiHoaDon()
        ));
        hoaDon.setGhiChu(request.getHoaDon().getGhiChu());
        hoaDon.setNguoiTao("admin"); // Default value

        // Save the invoice first to get the ID
        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Create ChiTietHoaDon records
        if (request.getChiTietHoaDon() != null && !request.getChiTietHoaDon().isEmpty()) {
            for (ChiTietHoaDonDTO chiTietDTO : request.getChiTietHoaDon()) {
                HoaDonCt hoaDonCt = new HoaDonCt();
                hoaDonCt.setHoaDon(savedHoaDon);

                // Get ChiTietSanPham
                ChiTietSanPham chiTietSanPham = chiTietSanPhamRepository.findById(chiTietDTO.getSanPhamId())
                        .orElseThrow(() -> new RuntimeException("Chi tiết sản phẩm không tồn tại: " + chiTietDTO.getSanPhamId()));
                hoaDonCt.setChiTietSanPham(chiTietSanPham);

                hoaDonCt.setDonGia(BigDecimal.valueOf(chiTietDTO.getDonGia()));
                hoaDonCt.setThanhTien(BigDecimal.valueOf(chiTietDTO.getThanhTien()));
                hoaDonCt.setTrangThai(1); // Active

                HoaDonCt savedHoaDonCt = hoaDonCtRepository.save(hoaDonCt);

                // Update stock quantity
                Integer currentQuantity = chiTietSanPham.getSoLuong();
                if (currentQuantity == null) {
                    currentQuantity = 0; // Default to 0 if null
                }
                int newQuantity = currentQuantity - chiTietDTO.getSoLuong();
                // Tạm thời cho phép bán âm kho để test
                // if (newQuantity < 0) {
                //     throw new RuntimeException("Không đủ hàng trong kho cho sản phẩm: " + chiTietSanPham.getSanPham().getTenSanPham());
                // }
                chiTietSanPham.setSoLuong(newQuantity);
                chiTietSanPhamRepository.save(chiTietSanPham);

                // Handle IMEI sales if IMEIs are provided
                System.out.println("Processing IMEIs for product: " + chiTietSanPham.getSanPham().getTenSanPham());
                System.out.println("Selected IMEIs: " + chiTietDTO.getSelectedImeis());
                
                if (chiTietDTO.getSelectedImeis() != null && !chiTietDTO.getSelectedImeis().isEmpty()) {
                    try {
                        System.out.println("Marking " + chiTietDTO.getSelectedImeis().size() + " IMEIs as sold");
                        for (String imei : chiTietDTO.getSelectedImeis()) {
                            System.out.println("Marking IMEI as sold: " + imei);
                            imeiDaBanService.markImeiAsSold(imei, savedHoaDonCt.getId());
                        }
                        System.out.println("Successfully marked all IMEIs as sold");
                    } catch (Exception e) {
                        System.out.println("Error marking IMEIs as sold: " + e.getMessage());
                        throw new RuntimeException("Lỗi khi xử lý IMEI: " + e.getMessage(), e);
                    }
                } else {
                    System.out.println("No IMEIs to process for this product");
                }
            }
        }

        return convertToDto(savedHoaDon);
    }

    @Override
    public HoaDonDTO updateHoaDon(Integer id, HoaDonDTO hoaDonDTO) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setKhachHangId(hoaDonDTO.getKhachHangId());
                    existingHoaDon.setNhanVienId(hoaDonDTO.getNhanVienId());
                    existingHoaDon.setPhieuGiamGiaId(hoaDonDTO.getPhieuGiamGiaId());
                    existingHoaDon.setTongTien(hoaDonDTO.getTongTien());
                    existingHoaDon.setTenKhachHang(hoaDonDTO.getTenKhachHang());
                    existingHoaDon.setSoDienThoai(hoaDonDTO.getSoDienThoai());
                    existingHoaDon.setDiaChi(hoaDonDTO.getDiaChi());
                    existingHoaDon.setTongTienSauGiam(hoaDonDTO.getTongTienSauGiam());
                    existingHoaDon.setNgayThanhToan(hoaDonDTO.getNgayThanhToan());
                    existingHoaDon.setLoaiHoaDon(hoaDonDTO.getLoaiHoaDon());
                    existingHoaDon.setTrangThai(hoaDonDTO.getTrangThai());
                    existingHoaDon.setGhiChu(hoaDonDTO.getGhiChu());
                    existingHoaDon.setNguoiCapNhat("admin"); // Update user
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public HoaDonDTO updateTrangThai(Integer id, Integer trangThai) {
        return hoaDonRepository.findById(id)
                .map(existingHoaDon -> {
                    existingHoaDon.setTrangThai(trangThai);
                    existingHoaDon.setNgayCapNhat(LocalDateTime.now());
                    return convertToDto(hoaDonRepository.save(existingHoaDon));
                })
                .orElseThrow(() -> new RuntimeException("Hóa đơn không tồn tại với id: " + id));
    }

    @Override
    public void deleteHoaDon(Integer id) {
        if (!hoaDonRepository.existsById(id)) {
            throw new RuntimeException("Hóa đơn không tồn tại với id: " + id);
        }
        hoaDonRepository.deleteById(id);
    }

    @Override
    public Long countByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return hoaDonRepository.countByNgayTaoBetween(startDate, endDate);
    }

    @Override
    public Double getTotalRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        Double revenue = hoaDonRepository.getTotalRevenueByDateRange(startDate, endDate);
        return revenue != null ? revenue : 0.0;
    }

    private HoaDonDTO convertToDto(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setKhachHangId(hoaDon.getKhachHangId());
        dto.setNhanVienId(hoaDon.getNhanVienId());
        dto.setPhieuGiamGiaId(hoaDon.getPhieuGiamGiaId());
        dto.setTongTien(hoaDon.getTongTien());
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setTongTienSauGiam(hoaDon.getTongTienSauGiam());
        dto.setNgayThanhToan(hoaDon.getNgayThanhToan());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
//        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setGhiChu(hoaDon.getGhiChu());
        dto.setNgayTao(hoaDon.getNgayTao());
        dto.setNgayCapNhat(hoaDon.getNgayCapNhat());
        dto.setNguoiTao(hoaDon.getNguoiTao());
        dto.setNguoiCapNhat(hoaDon.getNguoiCapNhat());
        return dto;
    }

    @Override
    public Page<HoaDonDTO> searchHoaDonAdvanced(HoaDonSearchRequestDTO searchRequest) {
        System.out.println("🚀 ===== searchHoaDonAdvanced METHOD CALLED =====");
        System.out.println("🔍 Search Request received: " + searchRequest);

        // Get ALL hóa đơn first (without pagination) - no initial sorting, will sort later
        List<HoaDon> allHoaDons = hoaDonRepository.findAll();
        System.out.println("📊 Total hóa đơn in database: " + allHoaDons.size());
        System.out.println("📊 Sample hóa đơn IDs: " + allHoaDons.stream().limit(10).map(h -> h.getId() + ":" + h.getMaHoaDon()).toList());

        // Debug: Check if we're getting all data
        if (allHoaDons.size() < 20) {
            System.out.println("⚠️ WARNING: Only " + allHoaDons.size() + " hóa đơn found, expected more!");
        }

        // Apply filters to get filtered list
        List<HoaDon> filteredHoaDons = allHoaDons.stream()
                .filter(hoaDon -> {
                    // Keyword filter
                    if (searchRequest.getKeyword() != null && !searchRequest.getKeyword().trim().isEmpty()) {
                        String keyword = searchRequest.getKeyword().toLowerCase();
                        boolean matchesKeyword =
                                hoaDon.getMaHoaDon().toLowerCase().contains(keyword) ||
                                        (hoaDon.getTenKhachHang() != null && hoaDon.getTenKhachHang().toLowerCase().contains(keyword)) ||
                                        (hoaDon.getSoDienThoai() != null && hoaDon.getSoDienThoai().contains(keyword));
                        if (!matchesKeyword) return false;
                    }

                    // Trạng thái filter
                    if (searchRequest.getTrangThai() != null) {
                        System.out.println("🔍 Filtering by status: " + searchRequest.getTrangThai() + " vs " + hoaDon.getTrangThai());
                        if (!hoaDon.getTrangThai().equals(searchRequest.getTrangThai())) return false;
                    }

                    // Loại hóa đơn filter
                    if (searchRequest.getLoaiHoaDon() != null && !searchRequest.getLoaiHoaDon().trim().isEmpty()) {
                        if (!hoaDon.getLoaiHoaDon().equals(searchRequest.getLoaiHoaDon())) return false;
                    }

                    // Date range filter
                    if (searchRequest.getTuNgay() != null) {
                        if (hoaDon.getNgayTao().isBefore(searchRequest.getTuNgay())) return false;
                    }

                    if (searchRequest.getDenNgay() != null) {
                        if (hoaDon.getNgayTao().isAfter(searchRequest.getDenNgay())) return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());

        System.out.println("📋 Filtered results: " + filteredHoaDons.size() + " items");
        System.out.println("📋 Sample filtered IDs: " + filteredHoaDons.stream().limit(5).map(h -> h.getId() + ":" + h.getMaHoaDon()).toList());

        // Debug: Show tongTien values before sorting
        System.out.println("💰 Before sorting - tongTien values:");
        filteredHoaDons.stream().limit(10).forEach(h -> {
            double displayValue = (h.getTongTienSauGiam() != null) ? h.getTongTienSauGiam() : h.getTongTien();
            System.out.println("  " + h.getMaHoaDon() + ": tongTien=" + h.getTongTien() + ", tongTienSauGiam=" + h.getTongTienSauGiam() + ", displayValue=" + displayValue);
        });

        // Apply sorting
        String sortBy = searchRequest.getSortBy() != null ? searchRequest.getSortBy() : "ngayTao";
        String sortDirection = searchRequest.getSortDirection() != null ? searchRequest.getSortDirection() : "desc";

        System.out.println("🔍 Sorting by: " + sortBy + " direction: " + sortDirection);

        filteredHoaDons.sort((a, b) -> {
            int result = 0;
            switch (sortBy) {
                case "maHoaDon":
                    result = a.getMaHoaDon().compareTo(b.getMaHoaDon());
                    break;
                case "tenKhachHang":
                    result = (a.getTenKhachHang() != null ? a.getTenKhachHang() : "").compareTo(b.getTenKhachHang() != null ? b.getTenKhachHang() : "");
                    break;
                case "tongTien":
                    // Sắp xếp theo giá trị thực tế được hiển thị (tongTienSauGiam hoặc tongTien)
                    double aValue = (a.getTongTienSauGiam() != null) ? a.getTongTienSauGiam() : a.getTongTien();
                    double bValue = (b.getTongTienSauGiam() != null) ? b.getTongTienSauGiam() : b.getTongTien();

                    // Handle null values
                    if (aValue == 0 && a.getTongTien() == null && a.getTongTienSauGiam() == null) aValue = 0.0;
                    if (bValue == 0 && b.getTongTien() == null && b.getTongTienSauGiam() == null) bValue = 0.0;

                    System.out.println("💰 Sorting: " + a.getMaHoaDon() + "(" + aValue + ") vs " + b.getMaHoaDon() + "(" + bValue + ")");
                    result = Double.compare(aValue, bValue);
                    break;
                case "ngayTao":
                default:
                    result = a.getNgayTao().compareTo(b.getNgayTao());
                    break;
            }
            return "desc".equals(sortDirection) ? -result : result;
        });

        // Debug: Show tongTien values after sorting
        System.out.println("💰 After sorting - tongTien values:");
        filteredHoaDons.stream().limit(10).forEach(h -> {
            double displayValue = (h.getTongTienSauGiam() != null) ? h.getTongTienSauGiam() : h.getTongTien();
            System.out.println("  " + h.getMaHoaDon() + ": displayValue=" + displayValue);
        });

        // Apply pagination manually
        int page = searchRequest.getPage() != null ? searchRequest.getPage() : 0;
        int size = searchRequest.getSize() != null ? searchRequest.getSize() : 10;
        int start = page * size;
        int end = Math.min(start + size, filteredHoaDons.size());

        List<HoaDon> pagedHoaDons = filteredHoaDons.subList(start, end);

        // Convert to DTO
        List<HoaDonDTO> dtoList = pagedHoaDons.stream()
                .map(this::convertToDto)
                .toList();

        System.out.println("📋 Final paginated results: " + dtoList.size() + " items");
        System.out.println("📋 Pagination info: page=" + page + ", size=" + size + ", total=" + filteredHoaDons.size());
        System.out.println("📋 Final response: totalPages=" + (int)Math.ceil((double)filteredHoaDons.size() / size) + ", totalElements=" + filteredHoaDons.size());

        // Create pageable for response
        Pageable pageable = PageRequest.of(page, size);

        return new PageImpl<>(dtoList, pageable, filteredHoaDons.size());
    }

    @Override
    public HoaDonTrackingDTO getTrackingInfo(String maHoaDon) {
        return hoaDonRepository.findByMaHoaDon(maHoaDon)
                .map(this::convertToTrackingDto)
                .orElse(null);
    }

    private HoaDonTrackingDTO convertToTrackingDto(HoaDon hoaDon) {
        HoaDonTrackingDTO dto = new HoaDonTrackingDTO();
        dto.setId(hoaDon.getId());
        dto.setMaHoaDon(hoaDon.getMaHoaDon());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setPhieuGiamGia(hoaDon.getPhieuGiamGiaId() != null ? "VC_" + hoaDon.getPhieuGiamGiaId() : null);
        dto.setNgayDat(hoaDon.getNgayTao());

        // Thông tin nhân viên
        dto.setNhanVienId(hoaDon.getNhanVienId());
        dto.setTenNhanVien(hoaDon.getNguoiTao() != null ? hoaDon.getNguoiTao() : "Không xác định");

        // Thông tin khách hàng
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setSoDienThoai(hoaDon.getSoDienThoai());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setGhiChu(hoaDon.getGhiChu());

        // Tổng kết đơn hàng
        dto.setTongTienHang(hoaDon.getTongTien() != null ? hoaDon.getTongTien() : 0.0);

        Double tongTienSauGiam = hoaDon.getTongTienSauGiam() != null ? hoaDon.getTongTienSauGiam() : hoaDon.getTongTien();
        dto.setGiamGia(hoaDon.getTongTien() != null && tongTienSauGiam != null ?
                hoaDon.getTongTien() - tongTienSauGiam : 0.0);
        dto.setThanhTien(tongTienSauGiam != null ? tongTienSauGiam : hoaDon.getTongTien());

        // TODO: Implement chi tiết sản phẩm và lịch sử trạng thái
        // This would require additional queries to get product details and status history
        dto.setDanhSachSanPham(new ArrayList<>());
        dto.setLichSuTrangThai(new ArrayList<>());

        return dto;
    }

    @Override
    @Transactional
    public void updateSampleDataWithDiverseStatuses() {
        // Cập nhật loại hóa đơn trước
        List<HoaDon> allHoaDons = hoaDonRepository.findAll();

        // Tạo map để track các mã hóa đơn đã xử lý
        Set<String> processedMaHoaDons = new HashSet<>();

        for (HoaDon hoaDon : allHoaDons) {
            // Bỏ qua nếu đã xử lý mã này rồi (tránh duplicate)
            if (processedMaHoaDons.contains(hoaDon.getMaHoaDon())) {
                continue;
            }

            // Cập nhật loại hóa đơn
            if (hoaDon.getLoaiHoaDon().equals("Bán lẻ") || hoaDon.getLoaiHoaDon().equals("Bán trực tiếp") || hoaDon.getLoaiHoaDon().equals("NORMAL")) {
                hoaDon.setLoaiHoaDon(OrderStatusUtil.NORMAL);
            } else if (hoaDon.getLoaiHoaDon().equals("Online") || hoaDon.getLoaiHoaDon().equals("Giao hàng") || hoaDon.getLoaiHoaDon().equals("DELIVERY")) {
                hoaDon.setLoaiHoaDon(OrderStatusUtil.DELIVERY);
            }

            // Cập nhật trạng thái đa dạng
            Integer currentStatus = hoaDon.getTrangThai();
            Integer newStatus = currentStatus % 6; // 0-5 để có đa dạng trạng thái
            hoaDon.setTrangThai(newStatus);

            hoaDonRepository.save(hoaDon);
            processedMaHoaDons.add(hoaDon.getMaHoaDon());
        }
    }

    @Override
    public List<Map<String, Object>> getHoaDonProducts(Integer hoaDonId) {
        try {
            // Tìm hóa đơn theo ID
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
            if (hoaDonOpt.isEmpty()) {
                return new ArrayList<>();
            }

            HoaDon hoaDon = hoaDonOpt.get();
            List<Map<String, Object>> products = new ArrayList<>();

            // Lấy danh sách chi tiết hóa đơn từ database
            List<HoaDonCt> hoaDonChiTiets = hoaDonCtRepository.findByIdHoaDon(hoaDonId);

            for (HoaDonCt hoaDonChiTiet : hoaDonChiTiets) {
                Map<String, Object> product = new HashMap<>();

                // Lấy thông tin chi tiết sản phẩm
                ChiTietSanPham chiTietSanPham = hoaDonChiTiet.getChiTietSanPham();
                if (chiTietSanPham != null) {
                    SanPham sanPham = chiTietSanPham.getSanPham();
                    if (sanPham != null) {
                        product.put("id", chiTietSanPham.getId());
                        // Tạo mã CTSP nếu không có hoặc rỗng
                        String maCtsp = chiTietSanPham.getMaCtsp();
                        if (maCtsp == null || maCtsp.trim().isEmpty()) {
                            maCtsp = "CTSP" + chiTietSanPham.getId();
                        }
                        product.put("maCtsp", maCtsp);
                        product.put("tenSanPham", sanPham.getTenSanPham());

                        // Lấy thông tin RAM, ROM, màu sắc
                        if (chiTietSanPham.getRam() != null) {
                            product.put("tenRam", chiTietSanPham.getRam().getTenRam());
                        }
                        if (chiTietSanPham.getRom() != null) {
                            product.put("tenRom", chiTietSanPham.getRom().getDungLuong());
                        }
                        if (chiTietSanPham.getMauSac() != null) {
                            product.put("tenMauSac", chiTietSanPham.getMauSac().getTenMau());
                        }

                        // Lấy giá từ hóa đơn chi tiết
                        product.put("donGia", hoaDonChiTiet.getDonGia());
                        product.put("thanhTien", hoaDonChiTiet.getThanhTien());

                        // Lấy số lượng (tạm thời để 1, có thể cần thêm field số lượng vào HoaDonCt)
                        product.put("soLuong", 1);

                        // Lấy danh sách IMEI đã bán
                        List<String> imeis = new ArrayList<>();
                        if (hoaDonChiTiet.getImeiDaBans() != null) {
                            for (ImeiDaBan imeiDaBan : hoaDonChiTiet.getImeiDaBans()) {
                                imeis.add(imeiDaBan.getImei());
                            }
                        }
                        product.put("imeis", imeis);

                        // Lấy hình ảnh sản phẩm (nếu có)
                        if (chiTietSanPham.getHinhAnhs() != null && !chiTietSanPham.getHinhAnhs().isEmpty()) {
                            product.put("hinhAnh", chiTietSanPham.getHinhAnhs().get(0).getUrlAnh());
                        } else {
                            product.put("hinhAnh", "/placeholder-product.png");
                        }

                        products.add(product);
                    }
                }
            }

            return products;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public HoaDonDTO updateTrangThaiByMaHoaDon(String maHoaDon, Integer trangThai) {
        try {
            Optional<HoaDon> hoaDonOpt = hoaDonRepository.findByMaHoaDon(maHoaDon);
            if (hoaDonOpt.isEmpty()) {
                throw new RuntimeException("Không tìm thấy hóa đơn với mã: " + maHoaDon);
            }

            HoaDon hoaDon = hoaDonOpt.get();
            hoaDon.setTrangThai(trangThai);
            hoaDon.setNgayCapNhat(LocalDateTime.now());

            HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);
            return convertToDto(savedHoaDon);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi cập nhật trạng thái hóa đơn: " + e.getMessage());
        }
    }
}
