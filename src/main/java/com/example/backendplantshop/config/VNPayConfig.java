package com.example.backendplantshop.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class VNPayConfig {
    
    @Value("${vnpay.tmn-code}")
    private String tmnCode;
    
    @Value("${vnpay.hash-secret}")
    private String hashSecret;
    
    @Value("${vnpay.url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String url;
    
    @Value("${vnpay.return-url}")
    private String returnUrl;
    
    @Value("${vnpay.ipn-url}")
    private String ipnUrl;
    
    @Value("${vnpay.version:2.1.0}")
    private String version;
    
    @Value("${vnpay.command:pay}")
    private String command;
    
    @Value("${vnpay.curr-code:VND}")
    private String currCode;
    
    @Value("${vnpay.locale:vn}")
    private String locale;
}

