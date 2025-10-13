package com.example.datn_sd28_2025.service;

import java.util.Map;

public interface ThongKeService {
    Map<String, Object> getDashboardStats();
    Map<String, Object> getRecentOrders(int page, int size);
}



