package com.example.datn_sd28_2025.service;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    /**
     * Decode QR code from image file
     * @param file MultipartFile containing the QR code image
     * @return Decoded text from QR code
     * @throws Exception if QR code cannot be decoded
     */
    public String decodeQRCode(MultipartFile file) throws Exception {
        System.out.println("Starting QR code decode from file...");
        try {
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) {
                throw new Exception("Cannot read image from file");
            }
            System.out.println("Image loaded successfully. Dimensions: " + image.getWidth() + "x" + image.getHeight());

            Object result = decodeQRCode(image);
            if (result instanceof String) {
                return (String) result;
            } else if (result instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, String> errorMap = (Map<String, String>) result;
                throw new Exception(errorMap.get("error"));
            } else {
                throw new Exception("Unexpected result type from decodeQRCode");
            }
        } catch (Exception e) {
            System.err.println("Error decoding QR code from file: " + e.getMessage());
            throw new Exception("Cannot decode QR code from image: " + e.getMessage(), e);
        }
    }

    /**
     * Decode QR code from BufferedImage
     * @param image BufferedImage containing the QR code
     * @return Decoded text from QR code or error map
     */
    public Object decodeQRCode(BufferedImage image) {
        System.out.println("Starting QR code decode from BufferedImage...");
        try {
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            QRCodeReader reader = new QRCodeReader();
            Result result = reader.decode(bitmap);

            String decodedText = result.getText();
            System.out.println("QR code decoded successfully. Text: " + decodedText);
            return decodedText;
        } catch (NotFoundException e) {
            System.err.println("QR code not found in image: " + e.getMessage());
            // Return empty result instead of throwing exception
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Không tìm thấy QR code trong ảnh. Vui lòng kiểm tra:");
            errorResult.put("suggestions", "1. QR code có rõ nét không? 2. Ánh sáng có đủ không? 3. Camera có focus đúng không?");
            errorResult.put("rawData", "");
            return errorResult;
        } catch (ChecksumException e) {
            System.err.println("QR code checksum error: " + e.getMessage());
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "QR code bị lỗi checksum. Có thể do ảnh bị mờ hoặc hỏng");
            errorResult.put("rawData", "");
            return errorResult;
        } catch (FormatException e) {
            System.err.println("QR code format error: " + e.getMessage());
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Định dạng QR code không đúng");
            errorResult.put("rawData", "");
            return errorResult;
        } catch (Exception e) {
            System.err.println("Unexpected error decoding QR code: " + e.getMessage());
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Lỗi không xác định khi decode QR code: " + e.getMessage());
            errorResult.put("rawData", "");
            return errorResult;
        }
    }

    /**
     * Parse CCCD QR code data
     * CCCD QR code format: ID|Name|DateOfBirth|Gender|Address|...
     * @param qrText Raw QR code text
     * @return Map containing parsed CCCD data
     */
    public Map<String, String> parseCCCDData(String qrText) {
        Map<String, String> cccdData = new HashMap<>();

        try {
            System.out.println("Parsing QR text: " + qrText);

            if (qrText != null && !qrText.trim().isEmpty()) {
                // Try different parsing methods

                // Method 1: Split by pipe character (|) - Standard CCCD format
                if (qrText.contains("|")) {
                    System.out.println("Trying pipe-separated format");
                    String[] parts = qrText.split("\\|");
                    System.out.println("Parts count: " + parts.length);
                    System.out.println("Parts: " + java.util.Arrays.toString(parts));

                    if (parts.length >= 5) {
                        // Try different mapping strategies based on data patterns
                        System.out.println("Trying different mapping strategies...");

                        // Strategy 1: Try to identify fields by content patterns
                        String cccd = parts[0].trim();
                        String field1 = parts[1].trim();
                        String field2 = parts[2].trim();
                        String field3 = parts[3].trim();
                        String field4 = parts[4].trim();

                        System.out.println("Field analysis:");
                        System.out.println("Field 1: " + field1 + " (is digits: " + field1.matches("\\d+") + ")");
                        System.out.println("Field 2: " + field2 + " (is digits: " + field2.matches("\\d+") + ")");
                        System.out.println("Field 3: " + field3 + " (is digits: " + field3.matches("\\d+") + ")");
                        System.out.println("Field 4: " + field4 + " (is digits: " + field4.matches("\\d+") + ")");

                        // Identify fields by content
                        String hoTen = "";
                        String ngaySinh = "";
                        String gioiTinh = "";
                        String diaChi = "";

                        // Find name (contains Vietnamese characters, not all digits)
                        for (int i = 1; i < parts.length; i++) {
                            String field = parts[i].trim();
                            if (!field.matches("\\d+") && field.length() > 2 &&
                                    (field.contains(" ") || field.matches(".*[àáạảãâầấậẩẫăằắặẳẵèéẹẻẽêềếệểễìíịỉĩòóọỏõôồốộổỗơờớợởỡùúụủũưừứựửữỳýỵỷỹđĐ].*"))) {
                                hoTen = field;
                                break;
                            }
                        }

                        // Find date (8 digits or DD/MM/YYYY format)
                        for (int i = 1; i < parts.length; i++) {
                            String field = parts[i].trim();
                            if (field.matches("\\d{8}") || field.matches("\\d{2}/\\d{2}/\\d{4}") ||
                                    field.matches("\\d{2}-\\d{2}-\\d{4}")) {
                                // Convert DDMMYYYY to yyyy-MM-dd format
                                if (field.matches("\\d{8}")) {
                                    String day = field.substring(0, 2);
                                    String month = field.substring(2, 4);
                                    String year = field.substring(4, 8);
                                    ngaySinh = year + "-" + month + "-" + day;
                                    System.out.println("Converted date from DDMMYYYY to yyyy-MM-dd: " + field + " -> " + ngaySinh);
                                } else {
                                    ngaySinh = field;
                                }
                                break;
                            }
                        }

                        // Find gender (Nam, Nữ, Male, Female, M, F, 1, 0)
                        for (int i = 1; i < parts.length; i++) {
                            String field = parts[i].trim();
                            if (field.matches("Nam|Nữ|Male|Female|M|F|1|0")) {
                                gioiTinh = field;
                                break;
                            }
                        }

                        // Find address (longest remaining field that's not name, date, gender, or old CMND)
                        String longestField = "";
                        for (int i = 1; i < parts.length; i++) {
                            String field = parts[i].trim();
                            if (!field.equals(hoTen) && !field.equals(ngaySinh) && !field.equals(gioiTinh) &&
                                    !field.matches("\\d{9,12}") && // Not old CMND or phone number
                                    field.length() > 5 && // Must be longer than 5 characters
                                    !field.matches("\\d+")) { // Not all digits
                                if (field.length() > longestField.length()) {
                                    longestField = field;
                                }
                            }
                        }
                        diaChi = longestField;

                        // If no address found, try to find any remaining field that looks like an address
                        if (diaChi.isEmpty()) {
                            for (int i = 1; i < parts.length; i++) {
                                String field = parts[i].trim();
                                if (!field.equals(hoTen) && !field.equals(ngaySinh) && !field.equals(gioiTinh) &&
                                        !field.matches("\\d{9,12}") && field.length() > 3) {
                                    diaChi = field;
                                    break;
                                }
                            }
                        }

                        // Apply the identified fields
                        cccdData.put("cccd", cccd);
                        cccdData.put("hoTen", hoTen);
                        cccdData.put("ngaySinh", ngaySinh);
                        cccdData.put("gioiTinh", gioiTinh);
                        cccdData.put("diaChi", diaChi);

                        System.out.println("Mapped fields:");
                        System.out.println("CCCD: " + cccd);
                        System.out.println("HoTen: " + hoTen);
                        System.out.println("NgaySinh: " + ngaySinh);
                        System.out.println("GioiTinh: " + gioiTinh);
                        System.out.println("DiaChi: " + diaChi);

                        // Debug: show all available fields
                        System.out.println("All available fields:");
                        for (int i = 0; i < parts.length; i++) {
                            System.out.println("  Field " + i + ": '" + parts[i].trim() + "'");
                        }

                        // Add more fields if available
                        if (parts.length > 5) {
                            cccdData.put("quocTich", parts[5].trim());
                        }
                        if (parts.length > 6) {
                            cccdData.put("noiCap", parts[6].trim());
                        }
                        if (parts.length > 7) {
                            cccdData.put("ngayCap", parts[7].trim());
                        }
                        cccdData.put("format", "PIPE_SEPARATED");
                        cccdData.put("partsCount", String.valueOf(parts.length));
                    } else {
                        cccdData.put("error", "Invalid pipe-separated format - not enough parts");
                        cccdData.put("partsCount", String.valueOf(parts.length));
                        cccdData.put("parts", java.util.Arrays.toString(parts));
                    }
                }
                // Method 2: Try JSON format
                else if (qrText.trim().startsWith("{") && qrText.trim().endsWith("}")) {
                    System.out.println("Trying JSON format");
                    cccdData.put("format", "JSON");
                    cccdData.put("rawJson", qrText);
                }
                // Method 3: Try comma-separated format
                else if (qrText.contains(",")) {
                    System.out.println("Trying comma-separated format");
                    String[] parts = qrText.split(",");
                    if (parts.length >= 5) {
                        cccdData.put("cccd", parts[0].trim());
                        cccdData.put("hoTen", parts[1].trim());
                        cccdData.put("ngaySinh", parts[2].trim());
                        cccdData.put("gioiTinh", parts[3].trim());
                        cccdData.put("diaChi", parts[4].trim());
                        cccdData.put("format", "COMMA_SEPARATED");
                    } else {
                        cccdData.put("error", "Invalid comma-separated format - not enough parts");
                    }
                }
                // Method 4: Try space-separated format
                else if (qrText.contains(" ")) {
                    System.out.println("Trying space-separated format");
                    String[] parts = qrText.split("\\s+");
                    if (parts.length >= 5) {
                        cccdData.put("cccd", parts[0].trim());
                        cccdData.put("hoTen", parts[1].trim());
                        cccdData.put("ngaySinh", parts[2].trim());
                        cccdData.put("gioiTinh", parts[3].trim());
                        cccdData.put("diaChi", parts[4].trim());
                        cccdData.put("format", "SPACE_SEPARATED");
                    } else {
                        cccdData.put("error", "Invalid space-separated format - not enough parts");
                    }
                }
                // Method 5: Unknown format - store as raw data
                else {
                    System.out.println("Unknown format - storing as raw data");
                    cccdData.put("format", "UNKNOWN");
                    cccdData.put("error", "Unknown QR code format");
                    cccdData.put("rawText", qrText);
                }

                // Always add raw data
                cccdData.put("rawData", qrText);
                cccdData.put("textLength", String.valueOf(qrText.length()));

            } else {
                cccdData.put("error", "Empty QR code text");
            }
        } catch (Exception e) {
            System.err.println("Error parsing CCCD data: " + e.getMessage());
            cccdData.put("rawData", qrText);
            cccdData.put("error", "Error parsing CCCD data: " + e.getMessage());
        }

        System.out.println("Parsed CCCD data: " + cccdData);
        return cccdData;
    }

    /**
     * Process CCCD QR code from uploaded image
     * @param file MultipartFile containing QR code image
     * @return Map containing parsed CCCD data
     * @throws Exception if processing fails
     */
    public Map<String, String> processCCCDQRCode(MultipartFile file) throws Exception {
        System.out.println("Starting CCCD QR code processing...");

        try {
            // Decode QR code
            String qrText = decodeQRCode(file);
            System.out.println("QR text decoded: " + qrText);

            // Parse CCCD data
            Map<String, String> cccdData = parseCCCDData(qrText);
            System.out.println("CCCD data parsed: " + cccdData);

            // Always add raw QR text to the result
            cccdData.put("rawData", qrText);
            cccdData.put("qrText", qrText);
            cccdData.put("qrTextLength", String.valueOf(qrText.length()));

            System.out.println("Final CCCD data: " + cccdData);
            return cccdData;
        } catch (Exception e) {
            System.err.println("Error in processCCCDQRCode: " + e.getMessage());
            Map<String, String> errorResult = new HashMap<>();
            errorResult.put("error", "Lỗi xử lý QR code: " + e.getMessage());
            errorResult.put("rawData", "");
            return errorResult;
        }
    }

    /**
     * Validate CCCD data format
     * @param cccdData Map containing CCCD data
     * @return true if data is valid, false otherwise
     */
    public boolean validateCCCDData(Map<String, String> cccdData) {
        // Check required fields
        String cccd = cccdData.get("cccd");
        String hoTen = cccdData.get("hoTen");
        String ngaySinh = cccdData.get("ngaySinh");
        String gioiTinh = cccdData.get("gioiTinh");
        String diaChi = cccdData.get("diaChi");

        // Basic validation
        if (cccd == null || cccd.trim().isEmpty()) return false;
        if (hoTen == null || hoTen.trim().isEmpty()) return false;
        if (ngaySinh == null || ngaySinh.trim().isEmpty()) return false;
        if (gioiTinh == null || gioiTinh.trim().isEmpty()) return false;
        if (diaChi == null || diaChi.trim().isEmpty()) return false;

        // Validate CCCD format (12 digits)
        if (!cccd.matches("\\d{12}")) return false;

        // Validate date format (DD/MM/YYYY)
        if (!ngaySinh.matches("\\d{2}/\\d{2}/\\d{4}")) return false;

        // Validate gender
        if (!gioiTinh.equals("Nam") && !gioiTinh.equals("Nữ")) return false;

        return true;
    }
}
