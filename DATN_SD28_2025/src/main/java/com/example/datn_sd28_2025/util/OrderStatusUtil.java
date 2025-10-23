package com.example.datn_sd28_2025.util;

public class OrderStatusUtil {

    // Order status constants (updated according to new logic)
    public static final int CHO_XAC_NHAN = 0;           // Chờ xác nhận (New order created, not yet confirmed)
    public static final int CHO_GIAO_HANG = 1;          // Chờ giao hàng (Confirmed, waiting for delivery)
    public static final int DANG_GIAO = 2;              // Đang giao (Being transported / In delivery)
    public static final int HOAN_THANH = 3;             // Hoàn thành (Delivered / Completed)
    public static final int DA_HUY = 4;                 // Đã hủy (Order cancelled)

    // Payment method constants
    public static final String CASH = "CASH";           // Tiền mặt
    public static final String COD = "COD";             // Thu hộ
    public static final String BANK_TRANSFER = "BANK_TRANSFER"; // Chuyển khoản
    public static final String CREDIT_CARD = "CREDIT_CARD";     // Thẻ tín dụng

    // Order type constants
    public static final String NORMAL = "NORMAL";       // Bán trực tiếp
    public static final String DELIVERY = "DELIVERY";   // Giao hàng

    /**
     * Get initial status based on order type
     */
    public static int getInitialStatus(String orderType) {
        if (NORMAL.equals(orderType)) {
            return HOAN_THANH; // Thanh toán trực tiếp -> Hoàn thành
        } else if (DELIVERY.equals(orderType)) {
            return CHO_XAC_NHAN; // Giao hàng -> Chờ xác nhận
        }
        return HOAN_THANH; // Default
    }

    /**
     * Get next status in the workflow
     */
    public static int getNextStatus(int currentStatus) {
        switch (currentStatus) {
            case CHO_XAC_NHAN:
                return CHO_GIAO_HANG; // Chờ xác nhận -> Chờ giao hàng
            case CHO_GIAO_HANG:
                return DANG_GIAO; // Chờ giao hàng -> Đang giao
            case DANG_GIAO:
                return HOAN_THANH; // Đang giao -> Hoàn thành
            default:
                return currentStatus; // No change
        }
    }

    /**
     * Get status name in Vietnamese
     */
    public static String getStatusName(int status) {
        switch (status) {
            case CHO_XAC_NHAN:
                return "Chờ xác nhận";
            case CHO_GIAO_HANG:
                return "Chờ giao hàng";
            case DANG_GIAO:
                return "Đang giao";
            case HOAN_THANH:
                return "Hoàn thành";
            case DA_HUY:
                return "Đã hủy";
            default:
                return "Không xác định";
        }
    }

    /**
     * Get payment method name in Vietnamese
     */
    public static String getPaymentMethodName(String paymentMethod) {
        switch (paymentMethod) {
            case CASH:
                return "Tiền mặt";
            case COD:
                return "Thu hộ (COD)";
            case BANK_TRANSFER:
                return "Chuyển khoản";
            case CREDIT_CARD:
                return "Thẻ tín dụng";
            default:
                return "Không xác định";
        }
    }

    /**
     * Check if status can be updated
     */
    public static boolean canUpdateStatus(int currentStatus, int newStatus) {
        // Can only move forward in workflow or cancel
        if (newStatus == DA_HUY) {
            return currentStatus != HOAN_THANH && currentStatus != DA_HUY;
        }

        // Can only move to next status or stay the same
        return newStatus == currentStatus || newStatus == getNextStatus(currentStatus);
    }
}


