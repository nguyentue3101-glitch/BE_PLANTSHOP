package com.example.backendplantshop.service.impl;

import com.example.backendplantshop.dto.request.momo.MoMoCallbackRequest;
import com.example.backendplantshop.entity.OrderDetails;
import com.example.backendplantshop.entity.Orders;
import com.example.backendplantshop.enums.OrderSatus;
import com.example.backendplantshop.enums.PaymentStatus;
import com.example.backendplantshop.mapper.OrderDetailMapper;
import com.example.backendplantshop.mapper.OrderMapper;
import com.example.backendplantshop.mapper.ProductMapper;
import com.example.backendplantshop.service.intf.DepositService;
import com.example.backendplantshop.service.intf.MoMoCallbackService;
import com.example.backendplantshop.service.intf.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MoMoCallBackService implements MoMoCallbackService {
    private final DepositService depositService;
    private final PaymentService paymentService;
    private final OrderMapper orderMapper;
    private final OrderDetailMapper orderDetailMapper;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public void handleOrderPaymentCallback(Integer orderId, MoMoCallbackRequest callbackRequest) {
        if (callbackRequest.getResultCode() != null && callbackRequest.getResultCode() == 0) {
            try {
                if (orderId != null) {
                    // Cập nhật payment status thành SUCCESS
                    paymentService.updatePaymentsByOrderId(orderId, PaymentStatus.SUCCESS);

                    // set pending để user có thể hủy nếu đơn chưa xác nhận
                    Orders order = orderMapper.findById(orderId);
                    if (order != null && order.getStatus() != OrderSatus.PENDING_CONFIRMATION) {
                        order.setStatus(OrderSatus.PENDING_CONFIRMATION);
                        order.setUpdated_at(LocalDateTime.now());
                        orderMapper.update(order);
                        log.info("Đã cập nhật trạng thái đơn hàng {} thành PENDING_CONFIRMATION sau khi thanh toán MoMo thành công", orderId);
                    }

                    log.info("Đã cập nhật payment status thành SUCCESS và order status thành PENDING_CONFIRMATION cho đơn hàng {} sau khi thanh toán MoMo thành công", orderId);
                }
            } catch (Exception e) {
                log.error("Lỗi khi cập nhật payment và order status từ callback: {}", e.getMessage(), e);
            }
        } else {
            log.warn("Thanh toán thất bại: orderId={}, message={}",
                    callbackRequest.getOrderId(), callbackRequest.getMessage());
            if (orderId != null) {
                paymentService.updatePaymentsByOrderId(orderId, PaymentStatus.FAILED);
            }
        }
    }

    @Override
    @Transactional
    public void handleDepositCallback(Integer orderId, MoMoCallbackRequest callbackRequest) {
        if (orderId == null) {
            log.warn("Không xác định được orderId cho giao dịch đặt cọc");
            return;
        }
        if (callbackRequest.getResultCode() != null && callbackRequest.getResultCode() == 0) {
            depositService.handleDepositSuccess(orderId, callbackRequest.getAmount(), callbackRequest.getTransId());
        } else {
            paymentService.updatePaymentsByOrderId(orderId, PaymentStatus.FAILED);
            Orders order = orderMapper.findById(orderId);
            if (order != null && order.getStatus() != OrderSatus.CANCELLED) {
                order.setStatus(OrderSatus.CANCELLED);
                order.setUpdated_at(LocalDateTime.now());
                orderMapper.update(order);
                log.info("Đã cập nhật trạng thái đơn hàng {} thành CANCEL khi thanh toán đặt cọc thất bại", orderId);
            }
            List<OrderDetails> orderDetails = orderDetailMapper.findByOrderId(orderId);
            for (OrderDetails orderDetail : orderDetails) {
                productMapper.restoreProductQuantity(orderDetail.getProduct_id(), orderDetail.getQuantity());
                log.info("Đã cộng lại {} sản phẩm (product_id: {}) vào kho khi hủy đơn {} (đơn chưa được xác nhận)",
                        orderDetail.getQuantity(), orderDetail.getProduct_id(), orderId);
            }

            log.info("Đã cập nhật payment status thành CANCELLED cho orderId={}", orderId);
            log.warn("Đặt cọc thất bại cho order {}: {}. Deposit record vẫn tồn tại với paid = 0",
                    orderId, callbackRequest.getMessage());
        }
    }
}
