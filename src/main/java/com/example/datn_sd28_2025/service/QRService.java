package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.QRDataDTO;

public interface QRService {

    /**
     * Xử lý dữ liệu QR code từ căn cước công dân
     * @param qrText Dữ liệu text từ QR code
     * @return QRDataDTO chứa thông tin đã parse
     */
    QRDataDTO processQRCode(String qrText);

    /**
     * Validate dữ liệu QR code
     * @param qrData Dữ liệu QR đã parse
     * @return true nếu hợp lệ, false nếu không
     */
    boolean validateQRData(QRDataDTO qrData);

    /**
     * Parse dữ liệu từ format QR code căn cước công dân Việt Nam
     * @param qrText Raw text từ QR code
     * @return QRDataDTO hoặc null nếu không parse được
     */
    QRDataDTO parseVietnamIDCardQR(String qrText);
}









