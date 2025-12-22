package com.example.backendplantshop.dto.response.vnpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateVNPayPaymentResponse {
    private String payUrl; // URL thanh toán VNPay
    private String orderId; // Mã đơn hàng
    private Long amount; // Số tiền
    private String message; // Thông báo
}

