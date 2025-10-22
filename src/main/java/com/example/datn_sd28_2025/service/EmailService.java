package com.example.datn_sd28_2025.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    @Autowired
    private JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mã OTP đặt lại mật khẩu - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào,
                
                Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản PhoniX Store.
                
                Mã OTP của bạn là: %s
                
                Mã này có hiệu lực trong 10 phút.
                
                Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, otpCode);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("OTP email sent successfully to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending OTP email: " + e.getMessage());
            throw new RuntimeException("Không thể gửi email. Vui lòng thử lại sau.");
        }
    }
    
    public void sendPasswordResetSuccessEmail(String toEmail, String userName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mật khẩu đã được đặt lại thành công - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Mật khẩu của bạn đã được đặt lại thành công.
                
                Nếu bạn không thực hiện thao tác này, vui lòng liên hệ với chúng tôi ngay lập tức.
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, userName);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Password reset success email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending password reset success email: " + e.getMessage());
            // Don't throw exception for success email
        }
    }
    
    public void sendEmployeeAccountInfo(String toEmail, String hoTen, String taiKhoan, String matKhau, String chucVu) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Thông tin tài khoản nhân viên - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Tài khoản nhân viên của bạn đã được tạo thành công trong hệ thống PhoniX Store.
                
                Thông tin đăng nhập:
                - Tài khoản: %s
                - Mật khẩu: %s
                - Chức vụ: %s
                
                Vui lòng đăng nhập và đổi mật khẩu ngay lập tức để bảo mật tài khoản.
                
                Link đăng nhập: http://localhost:5173/login
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, hoTen, taiKhoan, matKhau, chucVu);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Employee account info email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending employee account info email: " + e.getMessage());
            // Don't throw exception for notification email
        }
    }
    
    public void sendVoucherNotification(String toEmail, String customerName, String voucherCode, String voucherName, String discountAmount, String validFrom, String validTo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Bạn có voucher mới - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Chúc mừng! Bạn đã nhận được voucher giảm giá từ PhoniX Store.
                
                Thông tin voucher:
                - Tên voucher: %s
                - Mã voucher: %s
                - Giá trị giảm: %s
                - Có hiệu lực từ: %s
                - Hạn sử dụng đến: %s
                
                Vui lòng sử dụng voucher trước ngày hết hạn.
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, customerName, voucherName, voucherCode, discountAmount, validFrom, validTo);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Voucher notification email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending voucher notification email: " + e.getMessage());
        }
    }
    
    public void sendVoucherExpiredNotification(String toEmail, String customerName, String voucherCode, String voucherName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Voucher đã hết hạn - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Voucher của bạn đã hết hạn:
                - Tên voucher: %s
                - Mã voucher: %s
                
                Vui lòng kiểm tra và sử dụng voucher mới từ chúng tôi.
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, customerName, voucherName, voucherCode);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Voucher expired notification email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending voucher expired notification email: " + e.getMessage());
        }
    }
    
    public void sendVoucherUpdatedNotification(String toEmail, String customerName, String voucherCode, String voucherName, String newDiscountAmount, String validFrom, String validTo) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Voucher đã được cập nhật - PhoniX Store");
            
            String emailBody = String.format("""
                Xin chào %s,
                
                Voucher của bạn đã được cập nhật:
                - Tên voucher: %s
                - Mã voucher: %s
                - Giá trị giảm mới: %s
                - Có hiệu lực từ: %s
                - Hạn sử dụng đến: %s
                
                Vui lòng sử dụng voucher với thông tin mới.
                
                Trân trọng,
                Đội ngũ PhoniX Store
                """, customerName, voucherName, voucherCode, newDiscountAmount, validFrom, validTo);
            
            message.setText(emailBody);
            
            mailSender.send(message);
            System.out.println("Voucher updated notification email sent to: " + toEmail);
            
        } catch (Exception e) {
            System.err.println("Error sending voucher updated notification email: " + e.getMessage());
        }
    }
}