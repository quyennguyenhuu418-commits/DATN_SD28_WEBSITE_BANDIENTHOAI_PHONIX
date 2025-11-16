package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service xử lý logic thống kê và báo cáo
 */
@Service
public class ThongKeService {

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChiTietHoaDonRepository chiTietHoaDonRepository;

    /**
     * Lấy thống kê chi tiết đầy đủ theo khoảng thời gian
     */
    public Map<String, Object> layThongKeChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            LocalDateTime batDau = tuNgay.atStartOfDay();
            LocalDateTime ketThuc = denNgay.atTime(23, 59, 59);
            
            // === THỐNG KÊ CƠ BẢN ===
            BigDecimal tongDoanhThu = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgay, denNgay);
            Long tongDonHang = hoaDonRepository.countByNgayTaoBetweenAndTrangThai(batDau, ketThuc, 1);
            Long tongSanPhamBanRa = chiTietHoaDonRepository.sumSoLuongByDateRange(tuNgay, denNgay);
            Long tongKhachHangMoi = khachHangRepository.countByNgayTaoBetween(batDau, ketThuc);
            
            ketQua.put("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);
            ketQua.put("tongDonHang", tongDonHang != null ? tongDonHang : 0L);
            ketQua.put("tongSanPhamBanRa", tongSanPhamBanRa != null ? tongSanPhamBanRa : 0L);
            ketQua.put("tongKhachHangMoi", tongKhachHangMoi != null ? tongKhachHangMoi : 0L);
            
            // === THỐNG KÊ CHI TIẾT DOANH THU ===
            Map<String, Object> thongKeDoanhThu = layThongKeDoanhThuChiTiet(tuNgay, denNgay);
            ketQua.putAll(thongKeDoanhThu);
            
            // === THỐNG KÊ SẢN PHẨM CHI TIẾT ===
            Map<String, Object> thongKeSanPham = layThongKeSanPhamChiTiet(tuNgay, denNgay);
            ketQua.putAll(thongKeSanPham);
            
            // === THỐNG KÊ KHÁCH HÀNG CHI TIẾT ===
            Map<String, Object> thongKeKhachHang = layThongKeKhachHangChiTiet(tuNgay, denNgay);
            ketQua.putAll(thongKeKhachHang);
            
            // === THỐNG KÊ NHÂN VIÊN CHI TIẾT ===
            Map<String, Object> thongKeNhanVien = layThongKeNhanVienChiTiet(tuNgay, denNgay);
            ketQua.putAll(thongKeNhanVien);
            
            // === THỐNG KÊ THEO THỜI GIAN ===
            Map<String, Object> thongKeThoiGian = layThongKeTheoThoiGian(tuNgay, denNgay);
            ketQua.putAll(thongKeThoiGian);
            
            // === PHÂN TÍCH VÀ DỰ BÁO ===
            Map<String, Object> phanTichDuBao = layPhanTichDuBao(tuNgay, denNgay);
            ketQua.putAll(phanTichDuBao);
            
            // Thời gian cập nhật
            ketQua.put("thoiGianCapNhat", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê chi tiết: " + e.getMessage());
            return taoDuLieuMacDinh();
        }
    }

    /**
     * Lấy thống kê doanh thu theo ngày
     */
    public Map<String, Object> layThongKeDoanhThuTheoNgay(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            List<Map<String, Object>> duLieuBieuDo = hoaDonRepository.getRevenueByDateRangeCorrected(tuNgay, denNgay);
            
            ketQua.put("duLieuBieuDo", duLieuBieuDo);
            ketQua.put("tongDoanhThu", tinhTongDoanhThu(duLieuBieuDo));
            ketQua.put("doanhThuTrungBinh", tinhDoanhThuTrungBinh(duLieuBieuDo));
            ketQua.put("ngayCoDoanhThuCaoNhat", timNgayCoDoanhThuCaoNhat(duLieuBieuDo));
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu: " + e.getMessage());
            return Map.of("duLieuBieuDo", new ArrayList<>(), "tongDoanhThu", BigDecimal.ZERO);
        }
    }

    /**
     * Lấy thống kê sản phẩm bán chạy
     */
    public Map<String, Object> layThongKeSanPhamBanChay(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            List<Map<String, Object>> sanPhamBanChay = hoaDonRepository.getTopSellingProductsByDateRange(tuNgay, denNgay, 10);
            
            ketQua.put("sanPhamBanChay", sanPhamBanChay);
            ketQua.put("tongSanPham", sanPhamBanChay.size());
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê sản phẩm bán chạy: " + e.getMessage());
            return Map.of("sanPhamBanChay", new ArrayList<>(), "tongSanPham", 0);
        }
    }

    /**
     * Lấy thống kê nhân viên
     */
    public Map<String, Object> layThongKeNhanVien(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            List<Map<String, Object>> thongKeNhanVien = hoaDonRepository.getStaffPerformanceByDateRange(tuNgay, denNgay);
            
            ketQua.put("thongKeNhanVien", thongKeNhanVien);
            ketQua.put("tongNhanVien", thongKeNhanVien.size());
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê nhân viên: " + e.getMessage());
            return Map.of("thongKeNhanVien", new ArrayList<>(), "tongNhanVien", 0);
        }
    }

    /**
     * Tính tỷ lệ tăng trưởng
     */
    private Map<String, Object> tinhTyLeTangTruong(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Tính khoảng thời gian trước đó
            long soNgay = tuNgay.until(denNgay).getDays() + 1;
            LocalDate tuNgayTruoc = tuNgay.minusDays(soNgay);
            LocalDate denNgayTruoc = tuNgay.minusDays(1);
            
            // Doanh thu hiện tại
            BigDecimal doanhThuHienTai = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgay, denNgay);
            
            // Doanh thu kỳ trước
            BigDecimal doanhThuKyTruoc = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgayTruoc, denNgayTruoc);
            
            // Tính tỷ lệ tăng trưởng
            double tyLeTangTruong = 0.0;
            if (doanhThuKyTruoc != null && doanhThuKyTruoc.compareTo(BigDecimal.ZERO) > 0) {
                tyLeTangTruong = ((doanhThuHienTai.doubleValue() - doanhThuKyTruoc.doubleValue()) / doanhThuKyTruoc.doubleValue()) * 100;
            }
            
            ketQua.put("tyLeTangTruongDoanhThu", Math.round(tyLeTangTruong * 100.0) / 100.0);
            ketQua.put("doanhThuKyTruoc", doanhThuKyTruoc != null ? doanhThuKyTruoc : BigDecimal.ZERO);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi tính tỷ lệ tăng trưởng: " + e.getMessage());
            return Map.of("tyLeTangTruongDoanhThu", 0.0, "doanhThuKyTruoc", BigDecimal.ZERO);
        }
    }

    /**
     * Tính tổng doanh thu từ dữ liệu biểu đồ
     */
    private BigDecimal tinhTongDoanhThu(List<Map<String, Object>> duLieuBieuDo) {
        double sum = duLieuBieuDo.stream()
            .mapToDouble(item -> {
                Object revenue = item.get("revenue");
                if (revenue instanceof Number) {
                    return ((Number) revenue).doubleValue();
                }
                return 0.0;
            })
            .sum();
        return BigDecimal.valueOf(sum);
    }

    /**
     * Tính doanh thu trung bình
     */
    private BigDecimal tinhDoanhThuTrungBinh(List<Map<String, Object>> duLieuBieuDo) {
        if (duLieuBieuDo.isEmpty()) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal tongDoanhThu = tinhTongDoanhThu(duLieuBieuDo);
        BigDecimal doanhThuTrungBinh = tongDoanhThu.divide(BigDecimal.valueOf(duLieuBieuDo.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        return doanhThuTrungBinh;
    }

    /**
     * Tìm ngày có doanh thu cao nhất
     */
    private String timNgayCoDoanhThuCaoNhat(List<Map<String, Object>> duLieuBieuDo) {
        if (duLieuBieuDo.isEmpty()) {
            return "Không có dữ liệu";
        }
        
        Map<String, Object> ngayCaoNhat = duLieuBieuDo.stream()
            .max((a, b) -> {
                double revenueA = ((Number) a.get("revenue")).doubleValue();
                double revenueB = ((Number) b.get("revenue")).doubleValue();
                return Double.compare(revenueA, revenueB);
            })
            .orElse(duLieuBieuDo.get(0));
        
        return ngayCaoNhat.get("date").toString();
    }

    /**
     * Thống kê doanh thu chi tiết
     */
    private Map<String, Object> layThongKeDoanhThuChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Doanh thu theo ngày
            List<Map<String, Object>> doanhThuTheoNgay = hoaDonRepository.getRevenueByDateRangeCorrected(tuNgay, denNgay);
            ketQua.put("doanhThuTheoNgay", doanhThuTheoNgay);
            
            // Doanh thu theo tháng
            List<Map<String, Object>> doanhThuTheoThang = hoaDonRepository.getMonthlyRevenueData(tuNgay, denNgay);
            ketQua.put("doanhThuTheoThang", doanhThuTheoThang);
            
            // Tổng doanh thu
            BigDecimal tongDoanhThu = tinhTongDoanhThu(doanhThuTheoNgay);
            ketQua.put("tongDoanhThuChiTiet", tongDoanhThu);
            
            // Doanh thu trung bình
            BigDecimal doanhThuTrungBinh = tinhDoanhThuTrungBinh(doanhThuTheoNgay);
            ketQua.put("doanhThuTrungBinh", doanhThuTrungBinh);
            
            // Ngày có doanh thu cao nhất
            String ngayCaoNhat = timNgayCoDoanhThuCaoNhat(doanhThuTheoNgay);
            ketQua.put("ngayCoDoanhThuCaoNhat", ngayCaoNhat);
            
            // Tỷ lệ tăng trưởng
            Map<String, Object> tyLeTangTruong = tinhTyLeTangTruong(tuNgay, denNgay);
            ketQua.putAll(tyLeTangTruong);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê doanh thu chi tiết: " + e.getMessage());
            return Map.of("doanhThuTheoNgay", new ArrayList<>(), "tongDoanhThuChiTiet", BigDecimal.ZERO);
        }
    }

    /**
     * Thống kê sản phẩm chi tiết
     */
    private Map<String, Object> layThongKeSanPhamChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Top sản phẩm bán chạy
            List<Map<String, Object>> sanPhamBanChay = hoaDonRepository.getTopSellingProductsByDateRange(tuNgay, denNgay, 20);
            ketQua.put("sanPhamBanChayChiTiet", sanPhamBanChay);
            
            // Sản phẩm bán theo ngày
            List<Map<String, Object>> sanPhamTheoNgay = chiTietHoaDonRepository.getProductsChartData(tuNgay, denNgay);
            ketQua.put("sanPhamTheoNgay", sanPhamTheoNgay);
            
            // Tổng số sản phẩm bán ra
            Long tongSanPhamBanRa = chiTietHoaDonRepository.sumSoLuongByDateRange(tuNgay, denNgay);
            ketQua.put("tongSanPhamBanRaChiTiet", tongSanPhamBanRa != null ? tongSanPhamBanRa : 0L);
            
            // Số lượng sản phẩm khác nhau đã bán
            Long soSanPhamKhacNhau = chiTietHoaDonRepository.countDistinctSanPhamByDateRange(tuNgay, denNgay);
            ketQua.put("soSanPhamKhacNhau", soSanPhamKhacNhau != null ? soSanPhamKhacNhau : 0L);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê sản phẩm chi tiết: " + e.getMessage());
            return Map.of("sanPhamBanChayChiTiet", new ArrayList<>(), "tongSanPhamBanRaChiTiet", 0L);
        }
    }

    /**
     * Thống kê khách hàng chi tiết
     */
    private Map<String, Object> layThongKeKhachHangChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Khách hàng mới theo ngày
            List<Map<String, Object>> khachHangMoiTheoNgay = hoaDonRepository.getCustomersByDateRange(tuNgay, denNgay);
            ketQua.put("khachHangMoiTheoNgay", khachHangMoiTheoNgay);
            
            // Doanh thu từ khách hàng mới
            List<Map<String, Object>> doanhThuKhachHangMoi = hoaDonRepository.getCustomerRevenueByDateRange(tuNgay, denNgay);
            ketQua.put("doanhThuKhachHangMoi", doanhThuKhachHangMoi);
            
            // Top khách hàng VIP
            List<Map<String, Object>> khachHangVIP = hoaDonRepository.getVipCustomers(tuNgay, denNgay, 10);
            ketQua.put("khachHangVIP", khachHangVIP);
            
            // Số khách hàng khác nhau đã mua
            Long soKhachHangKhacNhau = hoaDonRepository.countDistinctCustomersByDateRange(tuNgay, denNgay);
            ketQua.put("soKhachHangKhacNhau", soKhachHangKhacNhau != null ? soKhachHangKhacNhau : 0L);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê khách hàng chi tiết: " + e.getMessage());
            return Map.of("khachHangMoiTheoNgay", new ArrayList<>(), "soKhachHangKhacNhau", 0L);
        }
    }

    /**
     * Thống kê nhân viên chi tiết
     */
    private Map<String, Object> layThongKeNhanVienChiTiet(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Hiệu suất nhân viên
            List<Map<String, Object>> hieuSuatNhanVien = hoaDonRepository.getStaffPerformanceByDateRange(tuNgay, denNgay);
            ketQua.put("hieuSuatNhanVien", hieuSuatNhanVien);
            
            // Tổng số nhân viên có hoạt động
            ketQua.put("tongNhanVienHoatDong", hieuSuatNhanVien.size());
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê nhân viên chi tiết: " + e.getMessage());
            return Map.of("hieuSuatNhanVien", new ArrayList<>(), "tongNhanVienHoatDong", 0);
        }
    }

    /**
     * Thống kê theo thời gian
     */
    private Map<String, Object> layThongKeTheoThoiGian(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Đơn hàng theo giờ
            List<Map<String, Object>> donHangTheoGio = hoaDonRepository.getOrdersByHour(tuNgay, denNgay);
            ketQua.put("donHangTheoGio", donHangTheoGio);
            
            // Đơn hàng theo ngày trong tuần
            List<Map<String, Object>> donHangTheoNgayTrongTuan = hoaDonRepository.getOrdersByDayOfWeek(tuNgay, denNgay);
            ketQua.put("donHangTheoNgayTrongTuan", donHangTheoNgayTrongTuan);
            
            // Trạng thái đơn hàng
            List<Map<String, Object>> trangThaiDonHang = hoaDonRepository.getOrderStatusData(tuNgay, denNgay);
            ketQua.put("trangThaiDonHang", trangThaiDonHang);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê theo thời gian: " + e.getMessage());
            return Map.of("donHangTheoGio", new ArrayList<>(), "trangThaiDonHang", new ArrayList<>());
        }
    }

    /**
     * Phân tích và dự báo
     */
    private Map<String, Object> layPhanTichDuBao(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Tính khoảng thời gian trước đó để so sánh
            long soNgay = tuNgay.until(denNgay).getDays() + 1;
            LocalDate tuNgayTruoc = tuNgay.minusDays(soNgay);
            LocalDate denNgayTruoc = tuNgay.minusDays(1);
            
            // So sánh với kỳ trước
            BigDecimal doanhThuHienTai = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgay, denNgay);
            BigDecimal doanhThuKyTruoc = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgayTruoc, denNgayTruoc);
            
            // Tính tỷ lệ tăng trưởng
            double tyLeTangTruong = 0.0;
            if (doanhThuKyTruoc != null && doanhThuKyTruoc.compareTo(BigDecimal.ZERO) > 0) {
                tyLeTangTruong = ((doanhThuHienTai.doubleValue() - doanhThuKyTruoc.doubleValue()) / doanhThuKyTruoc.doubleValue()) * 100;
            }
            
            ketQua.put("tyLeTangTruongDoanhThu", Math.round(tyLeTangTruong * 100.0) / 100.0);
            ketQua.put("doanhThuKyTruoc", doanhThuKyTruoc != null ? doanhThuKyTruoc : BigDecimal.ZERO);
            
            // Xu hướng (tăng/giảm/ổn định)
            String xuHuong = "Ổn định";
            if (tyLeTangTruong > 5) xuHuong = "Tăng trưởng";
            else if (tyLeTangTruong < -5) xuHuong = "Giảm";
            ketQua.put("xuHuong", xuHuong);
            
            // Dự báo doanh thu tháng tiếp theo (đơn giản)
            BigDecimal duBaoThangTiepTheo = doanhThuHienTai.multiply(BigDecimal.valueOf(1 + (tyLeTangTruong / 100)));
            ketQua.put("duBaoThangTiepTheo", duBaoThangTiepTheo);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy phân tích dự báo: " + e.getMessage());
            return Map.of("tyLeTangTruongDoanhThu", 0.0, "xuHuong", "Không xác định");
        }
    }
    
    /**
     * Lấy thống kê tồn kho
     */
    public Map<String, Object> layThongKeTonKho() {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Tổng số sản phẩm trong kho
            Long tongSanPham = sanPhamRepository.count();
            ketQua.put("tongSanPham", tongSanPham);
            
            // Sản phẩm sắp hết hàng (số lượng < 10)
            Long sanPhamSapHetHang = chiTietHoaDonRepository.countSanPhamSapHetHang();
            ketQua.put("sanPhamSapHetHang", sanPhamSapHetHang);
            
            // Sản phẩm hết hàng
            Long sanPhamHetHang = chiTietHoaDonRepository.countSanPhamHetHang();
            ketQua.put("sanPhamHetHang", sanPhamHetHang);
            
            // Giá trị tồn kho
            BigDecimal giaTriTonKho = chiTietHoaDonRepository.getGiaTriTonKho();
            ketQua.put("giaTriTonKho", giaTriTonKho != null ? giaTriTonKho : BigDecimal.ZERO);
            
            // Top sản phẩm tồn kho nhiều nhất
            List<Map<String, Object>> topTonKho = chiTietHoaDonRepository.getTopSanPhamTonKho(10);
            ketQua.put("topSanPhamTonKho", topTonKho);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê tồn kho: " + e.getMessage());
            return Map.of("tongSanPham", 0L, "sanPhamSapHetHang", 0L, "sanPhamHetHang", 0L);
        }
    }

    /**
     * Lấy thống kê theo thương hiệu
     */
    public Map<String, Object> layThongKeThuongHieu(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Doanh thu theo thương hiệu
            List<Map<String, Object>> doanhThuThuongHieu = hoaDonRepository.getRevenueByBrand(tuNgay, denNgay);
            ketQua.put("doanhThuThuongHieu", doanhThuThuongHieu);
            
            // Số lượng bán theo thương hiệu
            List<Map<String, Object>> soLuongThuongHieu = hoaDonRepository.getQuantityByBrand(tuNgay, denNgay);
            ketQua.put("soLuongThuongHieu", soLuongThuongHieu);
            
            // Thương hiệu bán chạy nhất
            List<Map<String, Object>> thuongHieuBanChay = hoaDonRepository.getTopBrands(tuNgay, denNgay, 5);
            ketQua.put("thuongHieuBanChay", thuongHieuBanChay);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê thương hiệu: " + e.getMessage());
            return Map.of("doanhThuThuongHieu", new ArrayList<>(), "soLuongThuongHieu", new ArrayList<>());
        }
    }

    /**
     * Lấy thống kê theo danh mục
     */
    public Map<String, Object> layThongKeDanhMuc(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Doanh thu theo danh mục
            List<Map<String, Object>> doanhThuDanhMuc = hoaDonRepository.getRevenueByCategory(tuNgay, denNgay);
            ketQua.put("doanhThuDanhMuc", doanhThuDanhMuc);
            
            // Số lượng bán theo danh mục
            List<Map<String, Object>> soLuongDanhMuc = hoaDonRepository.getQuantityByCategory(tuNgay, denNgay);
            ketQua.put("soLuongDanhMuc", soLuongDanhMuc);
            
            // Danh mục bán chạy nhất
            List<Map<String, Object>> danhMucBanChay = hoaDonRepository.getTopCategories(tuNgay, denNgay, 5);
            ketQua.put("danhMucBanChay", danhMucBanChay);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê danh mục: " + e.getMessage());
            return Map.of("doanhThuDanhMuc", new ArrayList<>(), "soLuongDanhMuc", new ArrayList<>());
        }
    }

    /**
     * Lấy danh sách khách hàng chi nhiều nhất (VIP customers)
     */
    public Map<String, Object> layKhachHangVIP(LocalDate tuNgay, LocalDate denNgay, int limit) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            System.out.println("=== DEBUG VIP CUSTOMERS ===");
            System.out.println("Date range: " + tuNgay + " to " + denNgay);
            System.out.println("Limit: " + limit);
            
            // Debug: Check raw data first
            List<Map<String, Object>> debugData = hoaDonRepository.debugHoaDonData(tuNgay, denNgay);
            System.out.println("Debug HoaDon Data: " + debugData);
            System.out.println("Debug Data count: " + (debugData != null ? debugData.size() : "null"));
            
            // Top khách hàng VIP theo tổng chi tiêu
            List<Map<String, Object>> khachHangVIP = hoaDonRepository.getVipCustomers(tuNgay, denNgay, limit);
            System.out.println("VIP Customers from DB: " + khachHangVIP);
            System.out.println("VIP Customers count: " + (khachHangVIP != null ? khachHangVIP.size() : "null"));
            
            ketQua.put("khachHangVIP", khachHangVIP);
            ketQua.put("soLuongKhachHang", khachHangVIP != null ? khachHangVIP.size() : 0);
            
            // Thống kê tổng quan
            if (!khachHangVIP.isEmpty()) {
                // Tổng chi tiêu của tất cả khách hàng VIP
                double tongChiTieu = khachHangVIP.stream()
                    .mapToDouble(customer -> {
                        Object totalSpent = customer.get("totalSpent");
                        if (totalSpent instanceof Number) {
                            return ((Number) totalSpent).doubleValue();
                        }
                        return 0.0;
                    })
                    .sum();
                ketQua.put("tongChiTieuVIP", tongChiTieu);
                
                // Chi tiêu trung bình
                double chiTieuTrungBinh = tongChiTieu / khachHangVIP.size();
                ketQua.put("chiTieuTrungBinh", Math.round(chiTieuTrungBinh * 100.0) / 100.0);
                
                // Khách hàng chi nhiều nhất
                Map<String, Object> khachHangTop1 = khachHangVIP.get(0);
                ketQua.put("khachHangTop1", khachHangTop1);
            }
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy khách hàng VIP: " + e.getMessage());
            return Map.of("khachHangVIP", new ArrayList<>(), "soLuongKhachHang", 0);
        }
    }

    /**
     * Lấy thống kê xu hướng mua hàng
     */
    public Map<String, Object> layThongKeXuHuong(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Xu hướng theo giờ trong ngày
            List<Map<String, Object>> xuHuongGio = hoaDonRepository.getTrendByHour(tuNgay, denNgay);
            ketQua.put("xuHuongGio", xuHuongGio);
            
            // Xu hướng theo ngày trong tuần
            List<Map<String, Object>> xuHuongNgay = hoaDonRepository.getTrendByDayOfWeek(tuNgay, denNgay);
            ketQua.put("xuHuongNgay", xuHuongNgay);
            
            // Xu hướng theo tháng
            List<Map<String, Object>> xuHuongThang = hoaDonRepository.getTrendByMonth(tuNgay, denNgay);
            ketQua.put("xuHuongThang", xuHuongThang);
            
            // Phân tích mùa vụ
            Map<String, Object> phanTichMuaVu = phanTichMuaVu(tuNgay, denNgay);
            ketQua.putAll(phanTichMuaVu);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê xu hướng: " + e.getMessage());
            return Map.of("xuHuongGio", new ArrayList<>(), "xuHuongNgay", new ArrayList<>());
        }
    }

    /**
     * Lấy thống kê tài chính
     */
    public Map<String, Object> layThongKeTaiChinh(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Tổng doanh thu
            BigDecimal tongDoanhThu = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgay, denNgay);
            ketQua.put("tongDoanhThu", tongDoanhThu != null ? tongDoanhThu : BigDecimal.ZERO);
            
            // Tổng chi phí (giá nhập)
            BigDecimal tongChiPhi = hoaDonRepository.getTotalCostByDateRange(tuNgay, denNgay);
            ketQua.put("tongChiPhi", tongChiPhi != null ? tongChiPhi : BigDecimal.ZERO);
            
            // Lợi nhuận
            BigDecimal loiNhuan = tongDoanhThu.subtract(tongChiPhi);
            ketQua.put("loiNhuan", loiNhuan);
            
            // Tỷ lệ lợi nhuận
            double tyLeLoiNhuan = 0.0;
            if (tongDoanhThu.compareTo(BigDecimal.ZERO) > 0) {
                tyLeLoiNhuan = (loiNhuan.doubleValue() / tongDoanhThu.doubleValue()) * 100;
            }
            ketQua.put("tyLeLoiNhuan", Math.round(tyLeLoiNhuan * 100.0) / 100.0);
            
            // Doanh thu trung bình mỗi đơn hàng
            Long tongDonHang = hoaDonRepository.countByNgayTaoBetweenAndTrangThai(
                tuNgay.atStartOfDay(), denNgay.atTime(23, 59, 59), 1);
            BigDecimal doanhThuTrungBinh = BigDecimal.ZERO;
            if (tongDonHang > 0) {
                doanhThuTrungBinh = tongDoanhThu.divide(BigDecimal.valueOf(tongDonHang), 2, BigDecimal.ROUND_HALF_UP);
            }
            ketQua.put("doanhThuTrungBinh", doanhThuTrungBinh);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi lấy thống kê tài chính: " + e.getMessage());
            return Map.of("tongDoanhThu", BigDecimal.ZERO, "tongChiPhi", BigDecimal.ZERO, "loiNhuan", BigDecimal.ZERO);
        }
    }

    /**
     * Phân tích mùa vụ
     */
    private Map<String, Object> phanTichMuaVu(LocalDate tuNgay, LocalDate denNgay) {
        Map<String, Object> ketQua = new HashMap<>();
        
        try {
            // Tính khoảng thời gian trước đó để so sánh
            long soNgay = tuNgay.until(denNgay).getDays() + 1;
            LocalDate tuNgayTruoc = tuNgay.minusDays(soNgay);
            LocalDate denNgayTruoc = tuNgay.minusDays(1);
            
            // Doanh thu hiện tại
            BigDecimal doanhThuHienTai = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgay, denNgay);
            
            // Doanh thu kỳ trước
            BigDecimal doanhThuKyTruoc = hoaDonRepository.getTotalRevenueByDateRangeCorrected(tuNgayTruoc, denNgayTruoc);
            
            // Phân tích mùa vụ
            String muaVu = "Bình thường";
            if (doanhThuHienTai.compareTo(doanhThuKyTruoc.multiply(BigDecimal.valueOf(1.2))) > 0) {
                muaVu = "Cao điểm";
            } else if (doanhThuHienTai.compareTo(doanhThuKyTruoc.multiply(BigDecimal.valueOf(0.8))) < 0) {
                muaVu = "Thấp điểm";
            }
            
            ketQua.put("muaVu", muaVu);
            ketQua.put("doanhThuKyTruoc", doanhThuKyTruoc != null ? doanhThuKyTruoc : BigDecimal.ZERO);
            
            return ketQua;
            
        } catch (Exception e) {
            System.err.println("Lỗi khi phân tích mùa vụ: " + e.getMessage());
            return Map.of("muaVu", "Không xác định", "doanhThuKyTruoc", BigDecimal.ZERO);
        }
    }

    /**
     * Tạo dữ liệu mặc định khi có lỗi
     */
    private Map<String, Object> taoDuLieuMacDinh() {
        Map<String, Object> ketQua = new HashMap<>();
        ketQua.put("tongDoanhThu", BigDecimal.ZERO);
        ketQua.put("tongDonHang", 0L);
        ketQua.put("tongSanPhamBanRa", 0L);
        ketQua.put("tongKhachHangMoi", 0L);
        ketQua.put("tyLeTangTruongDoanhThu", 0.0);
        ketQua.put("doanhThuKyTruoc", BigDecimal.ZERO);
        ketQua.put("thoiGianCapNhat", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        return ketQua;
    }
}
