package com.example.datn_sd28_2025.repository;

import com.example.datn_sd28_2025.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Lấy thông báo chưa đọc
    @Query("SELECT n FROM Notification n WHERE n.trangThai = 0 ORDER BY n.ngayTao DESC")
    List<Notification> findUnreadNotifications();
    
    // Lấy thông báo gần đây (cả đã đọc và chưa đọc)
    @Query("SELECT n FROM Notification n ORDER BY n.ngayTao DESC")
    List<Notification> findRecentNotifications();
    
    // Lấy thông báo theo loại
    @Query("SELECT n FROM Notification n WHERE n.loaiThongBao = :loai ORDER BY n.ngayTao DESC")
    List<Notification> findByLoaiThongBao(@Param("loai") String loai);
    
    // Đếm thông báo chưa đọc
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.trangThai = 0")
    Long countUnreadNotifications();
    
    // Lấy thông báo trong khoảng thời gian
    @Query("SELECT n FROM Notification n WHERE n.ngayTao BETWEEN :startDate AND :endDate ORDER BY n.ngayTao DESC")
    List<Notification> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                                      @Param("endDate") LocalDateTime endDate);
    
    // Đánh dấu tất cả thông báo là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.trangThai = 1, n.ngayCapNhat = :now WHERE n.trangThai = 0")
    int markAllAsRead(@Param("now") LocalDateTime now);
    
    // Đánh dấu thông báo cụ thể là đã đọc
    @Modifying
    @Query("UPDATE Notification n SET n.trangThai = 1, n.ngayCapNhat = :now WHERE n.id = :id")
    int markAsRead(@Param("id") Long id, @Param("now") LocalDateTime now);
}
