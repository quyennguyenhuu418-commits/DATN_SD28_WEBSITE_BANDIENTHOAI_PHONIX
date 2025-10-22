package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.entity.Notification;
import com.example.datn_sd28_2025.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    // Tạo thông báo mới
    public Notification createNotification(String tieuDe, String noiDung, String loaiThongBao, 
                                        Long idThamChieu, String duongDan) {
        Notification notification = new Notification();
        notification.setTieuDe(tieuDe);
        notification.setNoiDung(noiDung);
        notification.setLoaiThongBao(loaiThongBao);
        notification.setIdThamChieu(idThamChieu);
        notification.setDuongDan(duongDan);
        notification.setTrangThai(0); // Chưa đọc
        notification.setNgayTao(LocalDateTime.now());
        
        return notificationRepository.save(notification);
    }
    
    // Lấy danh sách thông báo chưa đọc
    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findUnreadNotifications();
    }
    
    // Lấy danh sách thông báo gần đây (tối đa 20 thông báo)
    public List<Notification> getRecentNotifications() {
        return notificationRepository.findRecentNotifications().stream()
                .limit(20)
                .toList();
    }
    
    // Đếm số thông báo chưa đọc
    public Long getUnreadCount() {
        return notificationRepository.countUnreadNotifications();
    }
    
    // Đánh dấu tất cả thông báo là đã đọc
    @Transactional
    public int markAllAsRead() {
        return notificationRepository.markAllAsRead(LocalDateTime.now());
    }
    
    // Đánh dấu thông báo cụ thể là đã đọc
    @Transactional
    public int markAsRead(Long id) {
        return notificationRepository.markAsRead(id, LocalDateTime.now());
    }
    
    // Lấy thông báo theo loại
    public List<Notification> getNotificationsByType(String loaiThongBao) {
        return notificationRepository.findByLoaiThongBao(loaiThongBao);
    }
    
    // Xóa thông báo cũ (trước 30 ngày)
    public void deleteOldNotifications() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        List<Notification> oldNotifications = notificationRepository.findByDateRange(
            LocalDateTime.of(2020, 1, 1, 0, 0), thirtyDaysAgo);
        notificationRepository.deleteAll(oldNotifications);
    }
    
    // Tạo thông báo đơn hàng mới
    public void createOrderNotification(String maHoaDon, String tenKhachHang, Double tongTien) {
        String tieuDe = "Đơn hàng mới: " + maHoaDon;
        String noiDung = String.format("Khách hàng %s đã đặt đơn hàng với tổng tiền %,.0f VNĐ", 
                                     tenKhachHang, tongTien);
        createNotification(tieuDe, noiDung, "ORDER_NEW", null, "/don-hang");
    }
    
    // Tạo thông báo sản phẩm mới
    public void createProductNotification(String tenSanPham, String danhMuc) {
        String tieuDe = "Sản phẩm mới: " + tenSanPham;
        String noiDung = String.format("Sản phẩm %s đã được thêm vào danh mục %s", 
                                     tenSanPham, danhMuc);
        createNotification(tieuDe, noiDung, "PRODUCT_NEW", null, "/san-pham");
    }
    
    // Tạo thông báo khách hàng mới
    public void createCustomerNotification(String tenKhachHang, String email) {
        String tieuDe = "Khách hàng mới: " + tenKhachHang;
        String noiDung = String.format("Khách hàng %s (%s) đã đăng ký tài khoản", 
                                     tenKhachHang, email);
        createNotification(tieuDe, noiDung, "CUSTOMER_NEW", null, "/khach-hang");
    }
    
    // Tạo thông báo nhân viên mới
    public void createEmployeeNotification(String tenNhanVien, String chucVu, String email) {
        String tieuDe = "Nhân viên mới: " + tenNhanVien;
        String noiDung = String.format("Nhân viên %s (%s) đã được thêm vào hệ thống với chức vụ %s", 
                                     tenNhanVien, email, chucVu != null ? chucVu : "Chưa xác định");
        createNotification(tieuDe, noiDung, "EMPLOYEE_NEW", null, "/nhan-vien");
    }
    
    // Tạo thông báo khách hàng cần tư vấn
    public void createCustomerChatNotification(String tenKhachHang, String soDienThoai, String tinNhan) {
        String tieuDe = "Khách hàng cần tư vấn: " + tenKhachHang;
        String noiDung = String.format("Khách hàng %s (%s) đang chờ hỗ trợ tư vấn. Tin nhắn: %s", 
                                     tenKhachHang, soDienThoai, tinNhan != null ? tinNhan : "Không có tin nhắn");
        createNotification(tieuDe, noiDung, "CUSTOMER_CHAT", null, "/staff-chat");
    }
    
    // Tạo thông báo hệ thống
    public void createSystemNotification(String tieuDe, String noiDung) {
        createNotification(tieuDe, noiDung, "SYSTEM", null, null);
    }
    
    // Lấy thống kê thông báo
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", notificationRepository.count());
        stats.put("unread", getUnreadCount());
        stats.put("recent", getRecentNotifications().size());
        return stats;
    }
}
