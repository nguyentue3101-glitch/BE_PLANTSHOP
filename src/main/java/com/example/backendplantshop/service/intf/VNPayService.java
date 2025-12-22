package com.example.backendplantshop.service.intf;

import com.example.backendplantshop.dto.request.vnpay.CreateVNPayPaymentRequest;
import com.example.backendplantshop.dto.response.vnpay.CreateVNPayPaymentResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface VNPayService {
    CreateVNPayPaymentResponse createPayment(CreateVNPayPaymentRequest request, HttpServletRequest httpRequest);
    boolean verifyCallback(String signature, java.util.Map<String, String> params);
}

