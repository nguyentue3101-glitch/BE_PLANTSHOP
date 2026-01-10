package com.example.backendplantshop.enums;

public enum MoMoPaymentPurpose {
    ORDER_PAYMENT,
    DEPOSIT;

    public static MoMoPaymentPurpose fromExtraData(String extraData) {
        if (extraData == null || extraData.isBlank()) {
            return ORDER_PAYMENT;
        }
        String normalized = extraData.trim().toUpperCase();
        //Kiểm tra xem chuỗi normalized có chứa từ "DEPOSIT" hay không
        if (normalized.contains("DEPOSIT")) {
            return DEPOSIT;
        }
        return ORDER_PAYMENT;
    }
}


