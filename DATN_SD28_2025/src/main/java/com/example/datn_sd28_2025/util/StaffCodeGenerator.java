package com.example.datn_sd28_2025.util;

import java.util.Random;

public class StaffCodeGenerator {
    
    public static String generateStaffCode(String hoTen) {
        if (hoTen == null || hoTen.trim().isEmpty()) {
            return "NV" + System.currentTimeMillis();
        }
        
        String cleanName = removeVietnameseAccents(hoTen.trim().toLowerCase());
        String[] nameParts = cleanName.split("\\s+");
        
        if (nameParts.length == 0) {
            return "NV" + System.currentTimeMillis();
        }
        
        StringBuilder maNhanVien = new StringBuilder();
        
        String ten = nameParts[nameParts.length - 1];
        if (!ten.isEmpty()) {
            maNhanVien.append(ten);
        }
        
        if (nameParts.length > 0 && !nameParts[0].isEmpty()) {
            maNhanVien.append(nameParts[0].charAt(0));
        }
        
        if (nameParts.length > 2 && !nameParts[1].isEmpty()) {
            maNhanVien.append(nameParts[1].charAt(0));
        } else if (nameParts.length == 2) {
            maNhanVien.append(nameParts[1].charAt(0));
        }
        
        Random random = new Random();
        int randomNumber = random.nextInt(900) + 100;
        maNhanVien.append(randomNumber);
        
        return maNhanVien.toString();
    }
    
    private static String removeVietnameseAccents(String text) {
        if (text == null) return "";
        
        return text
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("[đ]", "d")
                .replaceAll("[ÀÁẠẢÃÂẦẤẬẨẪĂẰẮẶẲẴ]", "A")
                .replaceAll("[ÈÉẸẺẼÊỀẾỆỂỄ]", "E")
                .replaceAll("[ÌÍỊỈĨ]", "I")
                .replaceAll("[ÒÓỌỎÕÔỒỐỘỔỖƠỜỚỢỞỠ]", "O")
                .replaceAll("[ÙÚỤỦŨƯỪỨỰỬỮ]", "U")
                .replaceAll("[ỲÝỴỶỸ]", "Y")
                .replaceAll("[Đ]", "D");
    }
    
    public static void main(String[] args) {
        String[] testNames = {
            "Nong Van Hung",
            "Nguyen Thi Mai",
            "Tran Duc Anh",
            "Le Van Nam",
            "Pham Thi Hoa",
            "Hoang Van Duc",
            "Vu Thi Lan",
            "Dang Van Minh",
            "Bui Thi Huong",
            "Ly Van Tung"
        };
        
        System.out.println("=== DEMO STAFF CODE GENERATION ===");
        System.out.println("Format: Name + First letter of surname + First letter of middle name + 2-3 numbers");
        System.out.println("Example: Nong Van Hung -> Hungnv123");
        System.out.println();
        
        for (String name : testNames) {
            String code = generateStaffCode(name);
            System.out.println(name + " -> " + code);
        }
        
        System.out.println();
        System.out.println("=== EDGE CASES ===");
        System.out.println("Empty name: '" + generateStaffCode("") + "'");
        System.out.println("Null name: '" + generateStaffCode(null) + "'");
        System.out.println("Single name: '" + generateStaffCode("Hung") + "'");
        System.out.println("Two names: '" + generateStaffCode("Van Hung") + "'");
        System.out.println("Vietnamese accents: '" + generateStaffCode("Nguyễn Văn Hùng") + "'");
    }
}
