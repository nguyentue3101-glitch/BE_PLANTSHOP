package com.example.backendplantshop.service.intf;

import com.example.backendplantshop.dto.request.momo.MoMoCallbackRequest;

public interface MoMoCallbackService {
    void handleDepositCallback(Integer orderId, MoMoCallbackRequest callbackRequest);
    void handleOrderPaymentCallback(Integer orderId, MoMoCallbackRequest callbackRequest);

}
