# 📘 Ví Dụ Code Frontend - VNPay Integration

## 🎯 Ví Dụ Hoàn Chỉnh

### 1. Store Pinia - `stores/paymentStore.js`

```javascript
import { defineStore } from 'pinia'
import { createVNPayPayment } from '@/services/paymentService'

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    isLoading: false,
    error: null
  }),

  actions: {
    async createVNPayPayment(paymentData) {
      this.isLoading = true
      this.error = null

      try {
        const { orderId, amount, orderInfo, purpose } = paymentData

        // Validate
        if (!orderId || orderId <= 0) {
          throw new Error('OrderId không hợp lệ')
        }
        if (!amount || amount < 1000) {
          throw new Error('Số tiền phải lớn hơn hoặc bằng 1000 VND')
        }

        // Gọi API
        const response = await createVNPayPayment({
          orderId: Number(orderId),
          amount: Math.round(Number(amount)),
          orderInfo: orderInfo || `Thanh toán đơn hàng #${orderId}`,
          purpose: purpose || 'ORDER_PAYMENT'
        })

        // Extract paymentUrl
        const paymentUrl = response?.data?.payUrl || 
                          response?.payUrl || 
                          response?.data?.paymentUrl

        if (!paymentUrl) {
          throw new Error('Không tìm thấy payment URL')
        }

        // Lưu orderId vào sessionStorage
        sessionStorage.setItem('vnpay_order_id', orderId.toString())

        // Redirect đến VNPay
        window.location.href = paymentUrl

      } catch (error) {
        console.error('VNPay payment error:', error)
        this.error = error.response?.data?.message || error.message
        throw error
      } finally {
        this.isLoading = false
      }
    }
  }
})
```

### 2. API Service - `services/paymentService.js`

```javascript
import apiClient from './apiClient'

export const createVNPayPayment = async (data) => {
  const response = await apiClient.post('/payments/vnpay/create', {
    orderId: Number(data.orderId),
    amount: Math.round(Number(data.amount)),
    orderInfo: data.orderInfo || `Thanh toán đơn hàng #${data.orderId}`,
    purpose: data.purpose || 'ORDER_PAYMENT'
  })
  
  return response
}
```

### 3. Component - `components/VNPayButton.vue`

```vue
<template>
  <button 
    @click="handlePayment"
    :disabled="isLoading"
    class="vnpay-button"
  >
    <span v-if="isLoading">Đang xử lý...</span>
    <span v-else>Thanh toán VNPay</span>
  </button>
</template>

<script setup>
import { computed } from 'vue'
import { usePaymentStore } from '@/stores/paymentStore'

const props = defineProps({
  orderId: {
    type: Number,
    required: true
  },
  amount: {
    type: Number,
    required: true
  }
})

const paymentStore = usePaymentStore()
const isLoading = computed(() => paymentStore.isLoading)

const handlePayment = async () => {
  try {
    await paymentStore.createVNPayPayment({
      orderId: props.orderId,
      amount: props.amount
    })
  } catch (error) {
    // Error đã được xử lý trong store
    console.error('Payment failed:', error)
  }
}
</script>
```

### 4. Return Page - `views/PaymentReturn.vue`

```vue
<template>
  <div class="payment-return">
    <div v-if="result">
      <div v-if="result.success" class="success">
        <h2>✅ Thanh toán thành công!</h2>
        <p>Đơn hàng #{{ result.orderId }}</p>
      </div>
      <div v-else class="failed">
        <h2>❌ Thanh toán thất bại</h2>
        <p>{{ result.message }}</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const result = ref(null)

onMounted(() => {
  const { paymentResult, orderId, responseCode, transactionStatus } = route.query

  if (paymentResult === 'true') {
    const isSuccess = responseCode === '00' && transactionStatus === '00'
    
    result.value = {
      success: isSuccess,
      orderId: orderId,
      message: isSuccess 
        ? 'Thanh toán thành công!' 
        : `Thanh toán thất bại. Mã: ${responseCode}`
    }
  }
})
</script>
```

---

## 🔗 Router Setup

```javascript
{
  path: '/payment/return',
  name: 'PaymentReturn',
  component: () => import('@/views/PaymentReturn.vue')
}
```

---

## 📝 Sử Dụng

```vue
<template>
  <VNPayButton :order-id="425" :amount="330000" />
</template>

<script setup>
import VNPayButton from '@/components/VNPayButton.vue'
</script>
```

