package com.example.backendplantshop.controller;

import com.example.backendplantshop.dto.request.PaymentDtoRequest;
import com.example.backendplantshop.dto.request.momo.CreatePaymentRequest;
import com.example.backendplantshop.dto.request.momo.MoMoCallbackRequest;
import com.example.backendplantshop.dto.request.vnpay.CreateVNPayPaymentRequest;
import com.example.backendplantshop.dto.response.ApiResponse;
import com.example.backendplantshop.dto.response.PaymentDtoResponse;
import com.example.backendplantshop.dto.response.momo.CreatePaymentResponse;
import com.example.backendplantshop.dto.response.vnpay.CreateVNPayPaymentResponse;
import com.example.backendplantshop.enums.ErrorCode;
import com.example.backendplantshop.enums.MoMoPaymentPurpose;
import com.example.backendplantshop.enums.VNPayPaymentPurpose;
import com.example.backendplantshop.config.MoMoConfig;
import com.example.backendplantshop.exception.AppException;
import com.example.backendplantshop.service.impl.MoMoCallBackService;
import com.example.backendplantshop.service.intf.MoMoService;
import com.example.backendplantshop.service.intf.OrderService;
import com.example.backendplantshop.service.intf.PaymentService;
import com.example.backendplantshop.service.intf.VNPayService;
import com.example.backendplantshop.service.impl.AuthServiceImpl;
import com.example.backendplantshop.util.MoMoUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {
    
    private final PaymentService paymentService;
    private final MoMoCallBackService moMoCallBackService;
    private final MoMoService momoService;
    private final VNPayService vnPayService;
    private final OrderService orderService;
    private final AuthServiceImpl authService;
    private final MoMoConfig momoConfig;
    
    @PostMapping("/create/{orderId}")
    public ApiResponse<PaymentDtoResponse> createPayment(
            @PathVariable("orderId") int orderId,
            @Valid @RequestBody PaymentDtoRequest request) {
        // Kiểm tra quyền truy cập
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) && !authService.isAdmin(role)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return ApiResponse.<PaymentDtoResponse>builder()
                .statusCode(ErrorCode.ADD_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.ADD_SUCCESSFULL.getMessage())
                .data(paymentService.createPayment(request, orderId))
                .build();
    }
    
    @GetMapping("/{paymentId}")
    public ApiResponse<PaymentDtoResponse> getPaymentById(@PathVariable("paymentId") int paymentId) {
        PaymentDtoResponse payment = paymentService.getPaymentById(paymentId);
        return ApiResponse.<PaymentDtoResponse>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(payment)
                .build();
    }
    
    @GetMapping("/order/{orderId}")
    public ApiResponse<List<PaymentDtoResponse>> getPaymentsByOrderId(@PathVariable("orderId") int orderId) {
        List<PaymentDtoResponse> payments = paymentService.getPaymentsByOrderId(orderId);
        return ApiResponse.<List<PaymentDtoResponse>>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(payments)
                .build();
    }
    
    @GetMapping("/get-all")
    public ApiResponse<List<PaymentDtoResponse>> getAllPayments() {
        // Chỉ admin mới được xem tất cả payments
        String role = authService.getCurrentRole();
        if (!authService.isAdmin(role)) {
            throw new com.example.backendplantshop.exception.AppException(ErrorCode.ACCESS_DENIED);
        }
        
        List<PaymentDtoResponse> payments = paymentService.getAllPayments();
        return ApiResponse.<List<PaymentDtoResponse>>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(payments)
                .build();
    }
    
    @PutMapping("/{paymentId}/status")
    public ApiResponse<PaymentDtoResponse> updatePaymentStatus(
            @PathVariable("paymentId") int paymentId,
            @RequestParam("status") com.example.backendplantshop.enums.PaymentStatus status) {
        // Kiểm tra quyền truy cập
        String role = authService.getCurrentRole();
        if (!authService.isAdmin(role)) {
            throw new com.example.backendplantshop.exception.AppException(ErrorCode.ACCESS_DENIED);
        }
        
        PaymentDtoResponse payment = paymentService.updatePaymentStatus(paymentId, status);
        return ApiResponse.<PaymentDtoResponse>builder()
                .statusCode(ErrorCode.UPDATE_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.UPDATE_SUCCESSFULL.getMessage())
                .data(payment)
                .build();
    }
    
    /**
     * Tạo payment request với MoMo
     */
    @PostMapping("/momo/create")
    public ApiResponse<CreatePaymentResponse> createMoMoPayment(@Valid @RequestBody CreatePaymentRequest request) {
        // Kiểm tra quyền truy cập
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) && !authService.isAdmin(role)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        
        CreatePaymentResponse response = momoService.createPayment(request);
        return ApiResponse.<CreatePaymentResponse>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(response)
                .build();
    }
    
    /**
     * Callback từ MoMo sau khi thanh toán
     */
    @PostMapping("/momo/callback")
    public ResponseEntity<?> momoCallback(@RequestBody MoMoCallbackRequest callbackRequest) {
        try {
            log.info("Nhận callback từ MoMo: orderId={}, resultCode={}, amount={}", 
                    callbackRequest.getOrderId(), callbackRequest.getResultCode(), callbackRequest.getAmount());
            
            // Tạo raw hash để verify signature
            String rawHash = MoMoUtil.createCallbackRawHash(
                    momoConfig.getAccessKey(),
                    String.valueOf(callbackRequest.getAmount()),
                    callbackRequest.getExtraData() != null ? callbackRequest.getExtraData() : "",
                    callbackRequest.getMessage() != null ? callbackRequest.getMessage() : "",
                    callbackRequest.getOrderId(),
                    callbackRequest.getOrderInfo() != null ? callbackRequest.getOrderInfo() : "",
                    callbackRequest.getOrderType() != null ? callbackRequest.getOrderType() : "",
                    callbackRequest.getPartnerCode(),
                    callbackRequest.getPayType() != null ? callbackRequest.getPayType() : "",
                    callbackRequest.getRequestId(),
                    String.valueOf(callbackRequest.getResponseTime()),
                    callbackRequest.getResultCode(),
                    callbackRequest.getTransId()
            );
            
            // Verify signature
            boolean isValid = momoService.verifyCallback(callbackRequest.getSignature(), rawHash);
            if (!isValid) {
                log.warn("Signature không hợp lệ từ MoMo callback");
                return ResponseEntity.badRequest().body("{\"status\":\"invalid_signature\"}");
            }
            
            Integer orderId = paymentService.extractOrderIdFromMoMoOrderId(callbackRequest.getOrderId());
            if (orderId == null) {
                log.error("Không thể parse orderId từ giá trị: {}", callbackRequest.getOrderId());
            }
            MoMoPaymentPurpose purpose = MoMoPaymentPurpose.fromExtraData(callbackRequest.getExtraData());

            // Xử lý kết quả thanh toán
            if (purpose == MoMoPaymentPurpose.DEPOSIT) {
                moMoCallBackService.handleDepositCallback(orderId, callbackRequest);
            } else {
                moMoCallBackService.handleOrderPaymentCallback(orderId, callbackRequest);
            }
            
            // Trả về response cho MoMo
            return ResponseEntity.ok().body("{\"status\":\"success\"}");
            
        } catch (Exception e) {
            log.error("Lỗi khi xử lý callback từ MoMo: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"status\":\"error\"}");
        }
    }
    
    /**
     * Return URL sau khi thanh toán (redirect từ MoMo)
     */
    @GetMapping("/momo/return")
    public ResponseEntity<?> momoReturn(
            @RequestParam(required = false) String orderId,
            @RequestParam(required = false) Integer resultCode,
            @RequestParam(required = false) String message) {
        
        log.info("Return từ MoMo: orderId={}, resultCode={}, message={}", orderId, resultCode, message);
        
        // URL encode các tham số để tránh lỗi Unicode trong HTTP header
        String encodedOrderId = orderId != null ? URLEncoder.encode(orderId, StandardCharsets.UTF_8) : "";
        String encodedResultCode = resultCode != null ? String.valueOf(resultCode) : "";
        String encodedMessage = message != null ? URLEncoder.encode(message, StandardCharsets.UTF_8) : "";
        
        // Redirect về trang chủ frontend với thông tin kết quả thanh toán trong query params
        // Frontend có thể đọc query params và hiển thị thông báo tương ứng
        String redirectUrl = String.format("https://fe-plantshop-backup.onrender.com/orders-page/?paymentResult=true&orderId=%s&resultCode=%s&message=%s",
                encodedOrderId,
                encodedResultCode,
                encodedMessage);
        
        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }
    
    /**
     * Tạo payment request với VNPay
     */
    @PostMapping("/vnpay/create")
    public ApiResponse<CreateVNPayPaymentResponse> createVNPayPayment(
            @Valid @RequestBody CreateVNPayPaymentRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        // Kiểm tra quyền truy cập
        String role = authService.getCurrentRole();
        if (!authService.isUser(role) && !authService.isAdmin(role)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        CreateVNPayPaymentResponse response = vnPayService.createPayment(request, httpRequest);
        return ApiResponse.<CreateVNPayPaymentResponse>builder()
                .statusCode(ErrorCode.CALL_API_SUCCESSFULL.getCode())
                .success(Boolean.TRUE)
                .message(ErrorCode.CALL_API_SUCCESSFULL.getMessage())
                .data(response)
                .build();
    }

    /**
     * IPN (Instant Payment Notification) từ VNPay sau khi thanh toán
     * VNPay gửi callback qua GET request với query params
     */
    @GetMapping("/vnpay/callback")
    public ResponseEntity<?> vnpayCallback(
            @RequestParam(required = false) String vnp_TmnCode,
            @RequestParam(required = false) String vnp_Amount,
            @RequestParam(required = false) String vnp_BankCode,
            @RequestParam(required = false) String vnp_BankTranNo,
            @RequestParam(required = false) String vnp_CardType,
            @RequestParam(required = false) String vnp_OrderInfo,
            @RequestParam(required = false) String vnp_TransactionNo,
            @RequestParam(required = false) String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_TransactionStatus,
            @RequestParam(required = false) String vnp_TxnRef,
            @RequestParam(required = false) String vnp_SecureHash,
            @RequestParam(required = false) String vnp_PayDate,
            @RequestParam(required = false) String vnp_TransactionDate) {
        try {
            log.info("Nhận callback từ VNPay: vnp_TxnRef={}, vnp_ResponseCode={}, vnp_TransactionStatus={}",
                    vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);

            // Tạo Map từ tất cả params để verify signature
            Map<String, String> params = new HashMap<>();
            if (vnp_TmnCode != null) params.put("vnp_TmnCode", vnp_TmnCode);
            if (vnp_Amount != null) params.put("vnp_Amount", vnp_Amount);
            if (vnp_BankCode != null) params.put("vnp_BankCode", vnp_BankCode);
            if (vnp_BankTranNo != null) params.put("vnp_BankTranNo", vnp_BankTranNo);
            if (vnp_CardType != null) params.put("vnp_CardType", vnp_CardType);
            if (vnp_OrderInfo != null) params.put("vnp_OrderInfo", vnp_OrderInfo);
            if (vnp_TransactionNo != null) params.put("vnp_TransactionNo", vnp_TransactionNo);
            if (vnp_ResponseCode != null) params.put("vnp_ResponseCode", vnp_ResponseCode);
            if (vnp_TransactionStatus != null) params.put("vnp_TransactionStatus", vnp_TransactionStatus);
            if (vnp_TxnRef != null) params.put("vnp_TxnRef", vnp_TxnRef);
            if (vnp_PayDate != null) params.put("vnp_PayDate", vnp_PayDate);
            if (vnp_TransactionDate != null) params.put("vnp_TransactionDate", vnp_TransactionDate);

            // Verify signature
            if (vnp_SecureHash != null && !vnp_SecureHash.isEmpty()) {
                boolean isValid = vnPayService.verifyCallback(vnp_SecureHash, params);
                if (!isValid) {
                    log.warn("Signature không hợp lệ từ VNPay callback");
                    return ResponseEntity.badRequest().body("{\"RspCode\":\"97\",\"Message\":\"Checksum failed\"}");
                }
            }

            // Parse orderId từ vnp_TxnRef (format: ORDER_123_1234567890 hoặc DEPOSIT_123_1234567890)
            Integer orderId = extractOrderIdFromVNPayTxnRef(vnp_TxnRef);
            if (orderId == null) {
                log.error("Không thể parse orderId từ vnp_TxnRef: {}", vnp_TxnRef);
                return ResponseEntity.badRequest().body("{\"RspCode\":\"01\",\"Message\":\"Order not found\"}");
            }

            // Kiểm tra kết quả thanh toán
            // vnp_ResponseCode = "00" và vnp_TransactionStatus = "00" nghĩa là thanh toán thành công
            boolean isSuccess = "00".equals(vnp_ResponseCode) && "00".equals(vnp_TransactionStatus);

            VNPayPaymentPurpose purpose = VNPayPaymentPurpose.fromOrderInfo(vnp_OrderInfo);

            // Xử lý kết quả thanh toán
            if (isSuccess) {
                if (purpose == VNPayPaymentPurpose.DEPOSIT) {
                    // Xử lý đặt cọc thành công
                    // TODO: Implement handleVNPayDepositCallback
                    log.info("VNPay deposit thành công cho orderId: {}", orderId);
                } else {
                    // Xử lý thanh toán đơn hàng thành công
                    // TODO: Implement handleVNPayOrderPaymentCallback
                    paymentService.updatePaymentsByOrderId(orderId, com.example.backendplantshop.enums.PaymentStatus.SUCCESS);
                    log.info("VNPay payment thành công cho orderId: {}", orderId);
                }
                return ResponseEntity.ok().body("{\"RspCode\":\"00\",\"Message\":\"Success\"}");
            } else {
                log.warn("VNPay payment thất bại: orderId={}, vnp_ResponseCode={}, vnp_TransactionStatus={}",
                        orderId, vnp_ResponseCode, vnp_TransactionStatus);
                if (purpose != VNPayPaymentPurpose.DEPOSIT) {
                    paymentService.updatePaymentsByOrderId(orderId, com.example.backendplantshop.enums.PaymentStatus.FAILED);
                }
                return ResponseEntity.ok().body("{\"RspCode\":\"01\",\"Message\":\"Transaction failed\"}");
            }

        } catch (Exception e) {
            log.error("Lỗi khi xử lý callback từ VNPay: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body("{\"RspCode\":\"99\",\"Message\":\"Unknown error\"}");
        }
    }

    /**
     * Return URL sau khi thanh toán (redirect từ VNPay)
     */
    @GetMapping("/vnpay/return")
    public ResponseEntity<?> vnpayReturn(
            @RequestParam(required = false) String vnp_TxnRef,
            @RequestParam(required = false) String vnp_ResponseCode,
            @RequestParam(required = false) String vnp_TransactionStatus,
            @RequestParam(required = false) String vnp_OrderInfo,
            @RequestParam(required = false) String vnp_Amount) {

        log.info("Return từ VNPay: vnp_TxnRef={}, vnp_ResponseCode={}, vnp_TransactionStatus={}",
                vnp_TxnRef, vnp_ResponseCode, vnp_TransactionStatus);

        // Parse orderId từ vnp_TxnRef
        Integer orderId = extractOrderIdFromVNPayTxnRef(vnp_TxnRef);

        // URL encode các tham số
        String encodedTxnRef = vnp_TxnRef != null ? URLEncoder.encode(vnp_TxnRef, StandardCharsets.UTF_8) : "";
        String encodedResponseCode = vnp_ResponseCode != null ? URLEncoder.encode(vnp_ResponseCode, StandardCharsets.UTF_8) : "";
        String encodedTransactionStatus = vnp_TransactionStatus != null ? URLEncoder.encode(vnp_TransactionStatus, StandardCharsets.UTF_8) : "";
        String encodedOrderId = orderId != null ? String.valueOf(orderId) : "";

        // Redirect về trang frontend với thông tin kết quả thanh toán
        String redirectUrl = String.format("http://localhost:3000/orders-page/?paymentResult=true&orderId=%s&txnRef=%s&responseCode=%s&transactionStatus=%s",
                encodedOrderId,
                encodedTxnRef,
                encodedResponseCode,
                encodedTransactionStatus);

        return ResponseEntity.status(302)
                .header("Location", redirectUrl)
                .build();
    }

    /**
     * Extract orderId từ VNPay TxnRef
     * Format: ORDER_123_1234567890 hoặc DEPOSIT_123_1234567890
     */
    private Integer extractOrderIdFromVNPayTxnRef(String txnRef) {
        if (txnRef == null || txnRef.isEmpty()) {
            return null;
        }
        try {
            // Tách theo dấu _
            String[] parts = txnRef.split("_");
            if (parts.length >= 2) {
                return Integer.parseInt(parts[1]);
            }
        } catch (NumberFormatException e) {
            log.error("Không thể parse orderId từ vnp_TxnRef: {}", txnRef, e);
        }
        return null;
    }
}
