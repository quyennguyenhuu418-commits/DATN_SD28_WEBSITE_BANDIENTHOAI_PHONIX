package com.example.datn_sd28_2025.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Store branding
    @Value("${email.store.name:PhoniX Store}")
    private String storeName;
    @Value("${email.store.logo:http://localhost:8080/uploads/Logo2.png}")
    private String storeLogo;
    @Value("${email.store.primaryColor:#FF6B35}")
    private String primaryColor;
    @Value("${email.store.signature:PhoniX Store Team}")
    private String signature;

    // Template paths
    @Value("${email.template.orderConfirmation:templates/email/order_confirmation.html}")
    private String orderConfirmationTemplate;
    @Value("${email.template.orderStatus:templates/email/order_status.html}")
    private String orderStatusTemplate;

    private static final Map<String, Long> recentSentKeys = new ConcurrentHashMap<>();
    private static final long DEDUP_WINDOW_MS = 120_000; // 2 minutes

    // ================== OTP ==================
    public void sendOtpEmail(String toEmail, String otpCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Mã OTP đặt lại mật khẩu - " + storeName);

            String emailBody = String.format("""
                    Xin chào,
                    
                    Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản %s.
                    
                    Mã OTP của bạn là: %s
                    
                    Mã này có hiệu lực trong 10 phút.
                    
                    Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.
                    
                    Trân trọng,
                    %s
                    """, storeName, otpCode, signature);

            message.setText(emailBody);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Không thể gửi email OTP. Vui lòng thử lại sau.");
        }
    }

    // ================== ORDER CONFIRMATION ==================
    public void sendOrderConfirmationEmail(String toEmail, String customerName, String orderCode, double totalAfterDiscount, String statusName) {
        if (isDuplicated("confirm:" + orderCode)) return;
        try {
            String subject = String.format("%s - Xác nhận đơn hàng %s", storeName, orderCode);
            String html = renderTemplate(orderConfirmationTemplate, mapBaseVars("Xác nhận đơn hàng", customerName, orderCode, statusName, totalAfterDiscount));
            sendHtmlWithEmbeddedLogo(toEmail, subject, html);
            markSent("confirm:" + orderCode);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== ORDER STATUS ==================
    public void sendOrderStatusEmail(String toEmail, String customerName, String orderCode, String statusName) {
        if (isDuplicated("status:" + orderCode + ":" + statusName)) return;
        try {
            String subject = String.format("%s - Cập nhật trạng thái đơn %s", storeName, orderCode);
            String html = renderTemplate(orderStatusTemplate, mapBaseVars("Cập nhật trạng thái đơn hàng", customerName, orderCode, statusName, null));
            sendHtmlWithEmbeddedLogo(toEmail, subject, html);
            markSent("status:" + orderCode + ":" + statusName);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================== CORE UTILS ==================
    private Map<String, String> mapBaseVars(String heading, String customerName, String orderCode, String statusName, Double totalAfterDiscount) {
        Map<String, String> m = new HashMap<>();
        m.put("storeName", storeName);
        m.put("primaryColor", primaryColor);
        m.put("signature", signature);
        m.put("heading", heading);
        m.put("customerName", safe(customerName));
        m.put("orderCode", safe(orderCode));
        m.put("statusName", safe(statusName));
        m.put("totalAfterDiscount", totalAfterDiscount == null ? "" : formatVnd(totalAfterDiscount));
        m.put("trackUrl", "http://localhost:5173/theo-doi-don-hang?orderId=" + safe(orderCode));
        return m;
    }

    private String renderTemplate(String path, Map<String, String> vars) {
        try {
            ClassPathResource res = new ClassPathResource(path);
            if (res.exists()) {
                try (InputStream is = res.getInputStream()) {
                    String tpl = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    for (Map.Entry<String, String> e : vars.entrySet()) {
                        tpl = tpl.replace("{{" + e.getKey() + "}}", e.getValue());
                    }
                    // Thay logo thành cid (ảnh nhúng)
                    tpl = tpl.replace("{{storeLogo}}", "cid:logoImage");
                    return tpl;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "<p>Không thể tải template email.</p>";
    }

    private void sendHtmlWithEmbeddedLogo(String to, String subject, String html) throws Exception {
        MimeMessage mime = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(html, true);

        // Gắn logo vào email (ảnh nhúng)
        File logoFile = new File("src/main/resources/static/uploads/Logo2.png");
        if (logoFile.exists()) {
            FileSystemResource res = new FileSystemResource(logoFile);
            helper.addInline("logoImage", res);
        }

        mailSender.send(mime);
    }

    private boolean isDuplicated(String key) {
        long now = System.currentTimeMillis();
        Long prev = recentSentKeys.get(key);
        return prev != null && (now - prev) < DEDUP_WINDOW_MS;
    }

    private void markSent(String key) {
        recentSentKeys.put(key, System.currentTimeMillis());
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    private String formatVnd(double v) {
        return String.format("%,.0f", v).replace(',', '.');
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
