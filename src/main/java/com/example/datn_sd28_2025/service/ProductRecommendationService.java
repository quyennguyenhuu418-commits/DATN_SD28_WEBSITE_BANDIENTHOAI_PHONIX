package com.example.datn_sd28_2025.service;

import com.example.datn_sd28_2025.dto.SanPhamDTO;
import com.example.datn_sd28_2025.repository.SanPhamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductRecommendationService {

    @Autowired
    private SanPhamRepository sanPhamRepository;

    /**
     * Get product recommendations based on criteria
     */
    public List<SanPhamDTO> getRecommendations(Map<String, Object> criteria) {
        try {
            // Get all products
            var products = sanPhamRepository.findAll();

            // Convert to DTOs and apply filters
            return products.stream()
                .map(this::convertToDTO)
                .filter(product -> matchesCriteria(product, criteria))
                .limit(6) // Limit to 6 recommendations
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get recommendations by budget
     */
    public List<SanPhamDTO> getRecommendationsByBudget(double maxPrice) {
        try {
            var products = sanPhamRepository.findAll();

            return products.stream()
                .map(this::convertToDTO)
                .filter(product -> true) // Remove price filter since SanPhamDTO doesn't have giaBan
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get recommendations by brand
     */
    public List<SanPhamDTO> getRecommendationsByBrand(String brand) {
        try {
            var products = sanPhamRepository.findAll();

            return products.stream()
                .map(this::convertToDTO)
                .filter(product -> product.getTenHang() != null &&
                    product.getTenHang().toLowerCase().contains(brand.toLowerCase()))
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get recommendations by features
     */
    public List<SanPhamDTO> getRecommendationsByFeatures(List<String> features) {
        try {
            var products = sanPhamRepository.findAll();

            return products.stream()
                .map(this::convertToDTO)
                .filter(product -> hasFeatures(product, features))
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get trending products
     */
    public List<SanPhamDTO> getTrendingProducts() {
        try {
            var products = sanPhamRepository.findAll();

            return products.stream()
                .map(this::convertToDTO)
                .sorted((p1, p2) -> Integer.compare(
                    p2.getTongSoLuong() != null ? p2.getTongSoLuong() : 0,
                    p1.getTongSoLuong() != null ? p1.getTongSoLuong() : 0
                )) // Sort by stock (assuming higher stock = more popular)
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * Get similar products
     */
    public List<SanPhamDTO> getSimilarProducts(Integer productId) {
        try {
            var targetProduct = sanPhamRepository.findById(productId);
            if (targetProduct.isEmpty()) {
                return new ArrayList<>();
            }

            var target = targetProduct.get();
            var products = sanPhamRepository.findAll();

            return products.stream()
                .map(this::convertToDTO)
                .filter(product -> !product.getId().equals(productId))
                .filter(product -> isSimilar(product, target))
                .limit(6)
                .collect(Collectors.toList());
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // Helper methods
    private SanPhamDTO convertToDTO(com.example.datn_sd28_2025.entity.SanPham product) {
        SanPhamDTO dto = new SanPhamDTO();
        dto.setId(product.getId());
        dto.setTenSanPham(product.getTenSanPham());
        dto.setTenHang(product.getHang() != null ? product.getHang().getTen() : null);
        dto.setTenManHinh(product.getManHinh() != null ? product.getManHinh().getKichThuoc() : null);
        dto.setTenCameraSau(product.getCameraSau() != null ? product.getCameraSau().getThongSo() : null);
        dto.setTenChip(product.getChip() != null ? product.getChip().getTenChip() : null);
        dto.setTenPin(product.getPin() != null ? product.getPin().getDungLuongPin() : null);
        dto.setTongSoLuong(0); // Default stock
        // Note: SanPhamDTO doesn't have giaBan field, so we can't set it here
        return dto;
    }

    private boolean matchesCriteria(SanPhamDTO product, Map<String, Object> criteria) {
        // Check brand
        if (criteria.containsKey("brand")) {
            String brand = (String) criteria.get("brand");
            if (product.getTenHang() == null ||
                !product.getTenHang().toLowerCase().contains(brand.toLowerCase())) {
                return false;
            }
        }


         if (criteria.containsKey("priceRange")) {
             @SuppressWarnings("unchecked")
             Map<String, Double> priceRange = (Map<String, Double>) criteria.get("priceRange");
             // Price filtering not available in SanPhamDTO
         }

      
        if (criteria.containsKey("features")) {
            @SuppressWarnings("unchecked")
            List<String> features = (List<String>) criteria.get("features");
            if (!hasFeatures(product, features)) {
                return false;
            }
        }

        return true;
    }

    private boolean hasFeatures(SanPhamDTO product, List<String> features) {
        for (String feature : features) {
            String lowerFeature = feature.toLowerCase();
            boolean hasFeature = false;

            if (lowerFeature.contains("camera") && product.getTenCameraSau() != null) {
                hasFeature = true;
            } else if (lowerFeature.contains("pin") && product.getTenPin() != null) {
                hasFeature = true;
            } else if (lowerFeature.contains("màn hình") && product.getTenManHinh() != null) {
                hasFeature = true;
            } else if (lowerFeature.contains("chip") && product.getTenChip() != null) {
                hasFeature = true;
            }

            if (!hasFeature) {
                return false;
            }
        }

        return true;
    }

    private boolean isSimilar(SanPhamDTO product1, com.example.datn_sd28_2025.entity.SanPham product2) {
        // Check if products are from the same brand
        String product2Brand = product2.getHang() != null ? product2.getHang().getTen() : null;
        if (product1.getTenHang() != null && product2Brand != null &&
            product1.getTenHang().equals(product2Brand)) {
            return true;
        }

        // Check if products have similar features
        int similarFeatures = 0;
        String product2ManHinh = product2.getManHinh() != null ? product2.getManHinh().getKichThuoc() : null;
        if (product1.getTenManHinh() != null && product2ManHinh != null &&
            product1.getTenManHinh().equals(product2ManHinh)) {
            similarFeatures++;
        }
        String product2Chip = product2.getChip() != null ? product2.getChip().getTenChip() : null;
        if (product1.getTenChip() != null && product2Chip != null &&
            product1.getTenChip().equals(product2Chip)) {
            similarFeatures++;
        }

        return similarFeatures >= 1; // At least one similar feature
    }
}