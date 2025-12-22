package com.example.backendplantshop.enums;

public enum VNPayPaymentPurpose {
    ORDER_PAYMENT,
    DEPOSIT;
    
    public static VNPayPaymentPurpose fromOrderInfo(String orderInfo) {
        if (orderInfo == null || orderInfo.isBlank()) {
            return ORDER_PAYMENT;
        }
        String normalized = orderInfo.trim().toUpperCase();
        if (normalized.contains("ĐẶT CỌC") || normalized.contains("DEPOSIT")) {
            return DEPOSIT;
        }
        return ORDER_PAYMENT;
    }
}

