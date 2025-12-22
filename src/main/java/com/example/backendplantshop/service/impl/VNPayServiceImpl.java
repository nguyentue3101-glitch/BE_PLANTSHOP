package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.config.VNPayConfig;
import com.example.backendplantshop.dto.request.vnpay.CreateVNPayPaymentRequest;
import com.example.backendplantshop.dto.response.vnpay.CreateVNPayPaymentResponse;
import com.example.backendplantshop.service.intf.VNPayService;
import com.example.backendplantshop.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VNPayServiceImpl implements VNPayService {

    private final VNPayConfig vnPayConfig;

    @Override
    public CreateVNPayPaymentResponse createPayment(
            CreateVNPayPaymentRequest request,
            HttpServletRequest httpRequest
    ) {

        /* ================== 1. TxnRef ================== */
        // Dùng orderId + timestamp (tránh trùng)
        String vnpTxnRef =
                request.getOrderId() + String.valueOf(System.currentTimeMillis());

        /* ================== 2. Amount (*100) ================== */
        long amount = request.getAmount().longValue() * 100;

        /* ================== 3. OrderInfo ================== */
        String orderInfo = "Thanh toan don hang " + request.getOrderId();

        /* ================== 4. CreateDate & ExpireDate ================== */
        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(cld.getTime());

        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());

        /* ================== 5. Params ================== */
        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnPayConfig.getVersion());
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnPayConfig.getTmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", vnpTxnRef);
        vnpParams.put("vnp_OrderInfo", orderInfo);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnPayConfig.getReturnUrl());
        vnpParams.put("vnp_IpAddr", getClientIpAddress(httpRequest));
        vnpParams.put("vnp_CreateDate", createDate);
        vnpParams.put("vnp_ExpireDate", expireDate);

//        if (vnPayConfig.getIpnUrl() != null) {
//            vnpParams.put("vnp_IpnUrl", vnPayConfig.getIpnUrl());
//        }

        /* ================== 6. Hash ================== */
        String hashData = VNPayUtil.buildHashData(vnpParams);

        String secureHash = VNPayUtil.hmacSHA512(
                vnPayConfig.getHashSecret(),
                hashData
        );

        vnpParams.put("vnp_SecureHash", secureHash);

        /* ================== 7. Build URL ================== */
        String paymentUrl =
                vnPayConfig.getUrl()
                        + "?"
                        + VNPayUtil.buildQueryString(vnpParams);

        log.info("VNPay Payment URL: {}", paymentUrl);

        /* ================== 8. Response ================== */
        return CreateVNPayPaymentResponse.builder()
                .payUrl(paymentUrl)
                .orderId(String.valueOf(request.getOrderId()))
                .amount(request.getAmount().longValue())
                .message("OK")
                .build();
    }

    @Override
    public boolean verifyCallback(String secureHash, Map<String, String> params) {
        return VNPayUtil.verifySignature(
                params,
                secureHash,
                vnPayConfig.getHashSecret()
        );
    }

    /* ================== IP ADDRESS ================== */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0];
        }

        ip = request.getRemoteAddr();

        // VNPay KHÔNG CHẤP NHẬN IPv6 localhost
        if ("0:0:0:0:0:0:0:1".equals(ip) || "::1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

}
