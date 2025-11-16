package com.example.datn_sd28_2025.service.impl;

import com.example.datn_sd28_2025.dto.AdministrativeDivisionDTO;
import com.example.datn_sd28_2025.service.VietnamAdministrativeService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class VietnamAdministrativeServiceImpl implements VietnamAdministrativeService {

    private final RestTemplate restTemplate;
    
    @Value("${vietnam.api.base-url:https://provinces.open-api.vn/api/v2}")
    private String baseUrl;
    
    // Cache for provinces to avoid repeated API calls
    private List<AdministrativeDivisionDTO> cachedProvinces = null;
    
    public VietnamAdministrativeServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate != null ? restTemplate : new RestTemplate();
    }
    
    public VietnamAdministrativeServiceImpl() {
        this.restTemplate = new RestTemplate();
    }

    @Override
    public List<AdministrativeDivisionDTO> getProvinces() {
        // Return cached data if available
        if (cachedProvinces != null) {
            System.out.println("Returning cached provinces: " + cachedProvinces.size());
            return cachedProvinces;
        }
        
        try {
            String url = baseUrl + "/p/";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling API: " + url);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response body: " + response.getBody());
            
            if (response.getBody() != null) {
                List<Map<String, Object>> results = (List<Map<String, Object>>) response.getBody();
                cachedProvinces = convertOpenApiV2ProvinceDTOs(results);
                System.out.println("Cached provinces: " + cachedProvinces.size());
                return cachedProvinces;
            }
        } catch (Exception e) {
            // Log error but don't print full stack trace for SSL certificate issues
            if (e.getMessage() != null && (e.getMessage().contains("PKIX") || e.getMessage().contains("certificate"))) {
                System.err.println("API certificate expired or invalid. Returning empty list.");
            } else {
                System.err.println("Error fetching provinces from API: " + e.getMessage());
            }
        }
        
        // Return empty list if API fails
        System.out.println("API failed, returning empty provinces list");
        return new ArrayList<>();
    }


    @Override
    public List<AdministrativeDivisionDTO> getWards(String provinceCode) {
        System.out.println("Getting wards for province code: " + provinceCode);
        try {
            // Sử dụng provinces.open-api.vn v2 để lấy phường/xã trực tiếp từ tỉnh
            String url = baseUrl + "/w/?province=" + provinceCode;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Accept", "application/json");
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling API for wards: " + url);
            ResponseEntity<List> response = restTemplate.exchange(url, HttpMethod.GET, entity, List.class);
            System.out.println("Wards response status: " + response.getStatusCode());
            
            if (response.getBody() != null) {
                List<Map<String, Object>> wards = (List<Map<String, Object>>) response.getBody();
                System.out.println("Total wards from API: " + wards.size());
                
                List<AdministrativeDivisionDTO> provinceWards = new ArrayList<>();
                for (Map<String, Object> ward : wards) {
                    provinceWards.add(AdministrativeDivisionDTO.builder()
                            .code(String.valueOf(ward.get("code")))
                            .name((String) ward.get("name"))
                            .type((String) ward.get("division_type"))
                            .parentCode(provinceCode)
                            .level("WARD")
                            .build());
                }
                System.out.println("Wards found for province " + provinceCode + ": " + provinceWards.size());
                return provinceWards;
            }
        } catch (Exception e) {
            // Log error but don't print full stack trace for SSL certificate issues
            if (e.getMessage() != null && (e.getMessage().contains("PKIX") || e.getMessage().contains("certificate"))) {
                System.err.println("API certificate expired or invalid. Returning empty list.");
            } else {
                System.err.println("Error fetching wards from API: " + e.getMessage());
            }
        }
        
        // Return empty list if API fails
        System.out.println("API failed, returning empty wards list for province: " + provinceCode);
        return new ArrayList<>();
    }
    





    
    private List<AdministrativeDivisionDTO> convertOpenApiV2ProvinceDTOs(List<Map<String, Object>> provinces) {
        return provinces.stream()
                .map(province -> AdministrativeDivisionDTO.builder()
                        .code(String.valueOf(province.get("code")))
                        .name((String) province.get("name"))
                        .type((String) province.get("division_type"))
                        .parentCode(null)
                        .level("PROVINCE")
                        .build())
                .toList();
    }
    
}
