# 📘 Hướng Dẫn Tích Hợp VNPay - Vue 3 + Pinia

## 📋 Mục Lục

1. [Tổng Quan Flow Thanh Toán](#tổng-quan-flow-thanh-toán)
2. [Cấu Trúc Store Pinia](#cấu-trúc-store-pinia)
3. [API Service](#api-service)
4. [Component Vue](#component-vue)
5. [Xử Lý Return URL](#xử-lý-return-url)
6. [Error Handling](#error-handling)

---

## 🔄 Tổng Quan Flow Thanh Toán

```
1. User click "Thanh toán VNPay"
   ↓
2. Frontend gọi API: POST /api/payments/vnpay/create
   ↓
3. Backend trả về paymentUrl
   ↓
4. Frontend redirect user đến paymentUrl (VNPay)
   ↓
5. User thanh toán trên VNPay
   ↓
6. VNPay redirect về: /api/payments/vnpay/return
   ↓
7. Backend xử lý và redirect về Frontend: /orders-page/?paymentResult=true&...
   ↓
8. Frontend hiển thị kết quả thanh toán
```

---

## 🗂️ Cấu Trúc Store Pinia

### File: `stores/paymentStore.js`

```javascript
import { defineStore } from 'pinia'
import { createVNPayPayment } from '@/services/paymentService'
import { useRouter } from 'vue-router'

export const usePaymentStore = defineStore('payment', {
  state: () => ({
    isLoading: false,
    error: null,
    paymentUrl: null
  }),

  actions: {
    /**
     * Tạo payment request với VNPay
     * @param {Object} paymentData - { orderId, amount, orderInfo?, purpose? }
     * @returns {Promise<string>} Payment URL từ VNPay
     */
    async createVNPayPayment(paymentData) {
      this.isLoading = true
      this.error = null
      this.paymentUrl = null

      try {
        const { orderId, amount, orderInfo, purpose } = paymentData

        // Validate dữ liệu
        if (!orderId || orderId <= 0) {
          throw new Error('OrderId không hợp lệ')
        }
        if (!amount || amount <= 0) {
          throw new Error('Amount không hợp lệ')
        }
        if (amount < 1000) {
          console.warn('⚠️ Amount nhỏ hơn 1000 VND, VNPay có thể từ chối')
        }

        console.log('💳 Creating VNPay payment:', {
          orderId,
          amount,
          orderInfo: orderInfo || `Thanh toán đơn hàng #${orderId}`,
          purpose: purpose || 'ORDER_PAYMENT'
        })

        // Gọi API tạo payment
        const response = await createVNPayPayment({
          orderId: Number(orderId),
          amount: Math.round(Number(amount)), // VNPay yêu cầu số nguyên
          orderInfo: orderInfo || `Thanh toán đơn hàng #${orderId}`,
          purpose: purpose || 'ORDER_PAYMENT'
        })

        console.log('📥 VNPay payment response:', response)

        // Extract payment URL từ response
        // Backend trả về format: { success: true, data: { payUrl: "..." } }
        const paymentUrl = response?.data?.payUrl || 
                          response?.payUrl || 
                          response?.data?.paymentUrl ||
                          response?.paymentUrl

        if (!paymentUrl || typeof paymentUrl !== 'string') {
          throw new Error('Không tìm thấy payment URL từ response')
        }

        this.paymentUrl = paymentUrl
        console.log('✅ Payment URL received:', paymentUrl)

        return paymentUrl

      } catch (error) {
        console.error('❌ Create VNPay payment error:', error)
        this.error = error.response?.data?.message || 
                    error.message || 
                    'Không thể tạo thanh toán VNPay. Vui lòng thử lại.'
        throw error
      } finally {
        this.isLoading = false
      }
    },

    /**
     * Redirect đến VNPay payment page
     * @param {string} paymentUrl - URL thanh toán từ VNPay
     * @param {number} orderId - Mã đơn hàng (để lưu vào sessionStorage)
     */
    redirectToVNPay(paymentUrl, orderId) {
      if (!paymentUrl) {
        throw new Error('Payment URL không hợp lệ')
      }

      // Lưu orderId vào sessionStorage để xử lý sau khi return
      if (orderId) {
        sessionStorage.setItem('vnpay_payment_order_id', orderId.toString())
        sessionStorage.setItem('vnpay_payment_timestamp', Date.now().toString())
      }

      console.log('🔗 Redirecting to VNPay:', paymentUrl)
      
      // Redirect đến VNPay
      window.location.href = paymentUrl
    },

    /**
     * Xử lý kết quả thanh toán từ VNPay return URL
     * @param {Object} queryParams - Query params từ URL return
     * @returns {Object} Kết quả thanh toán { success, orderId, message }
     */
    handleVNPayReturn(queryParams) {
      const { 
        paymentResult, 
        orderId, 
        txnRef, 
        responseCode, 
        transactionStatus 
      } = queryParams

      console.log('📥 VNPay return params:', queryParams)

      // Kiểm tra có phải return từ VNPay không
      if (paymentResult !== 'true') {
        return {
          success: false,
          message: 'Không phải kết quả thanh toán từ VNPay'
        }
      }

      // Parse orderId từ txnRef hoặc query param
      const parsedOrderId = orderId || this.extractOrderIdFromTxnRef(txnRef)

      // VNPay response code:
      // - "00" = Thành công
      // - Khác "00" = Thất bại
      const isSuccess = responseCode === '00' && transactionStatus === '00'

      return {
        success: isSuccess,
        orderId: parsedOrderId,
        txnRef,
        responseCode,
        transactionStatus,
        message: isSuccess 
          ? 'Thanh toán thành công!' 
          : `Thanh toán thất bại. Mã lỗi: ${responseCode}`
      }
    },

    /**
     * Extract orderId từ vnp_TxnRef
     * Format: orderId_timestamp (ví dụ: 425_1766294559511)
     */
    extractOrderIdFromTxnRef(txnRef) {
      if (!txnRef) return null
      try {
        const parts = txnRef.split('_')
        if (parts.length >= 1) {
          return parseInt(parts[0])
        }
      } catch (error) {
        console.error('Error parsing orderId from txnRef:', error)
      }
      return null
    },

    /**
     * Clear payment state
     */
    clearPayment() {
      this.paymentUrl = null
      this.error = null
      this.isLoading = false
    }
  }
})
```

---

## 🔌 API Service

### File: `services/paymentService.js`

```javascript
import apiClient from './apiClient'

/**
 * Tạo payment request với VNPay
 * 
 * @param {Object} data - { orderId, amount, orderInfo?, purpose? }
 * @returns {Promise<Object>} Response chứa paymentUrl
 */
export const createVNPayPayment = async (data) => {
  try {
    // Validate
    if (!data.orderId || data.orderId <= 0) {
      throw new Error('OrderId không hợp lệ')
    }
    if (!data.amount || data.amount <= 0) {
      throw new Error('Amount không hợp lệ')
    }

    // VNPay yêu cầu amount là số nguyên (VND)
    const requestData = {
      orderId: Number(data.orderId),
      amount: Math.round(Number(data.amount)),
      orderInfo: data.orderInfo || `Thanh toán đơn hàng #${data.orderId}`,
      purpose: data.purpose || 'ORDER_PAYMENT'
    }

    console.log('📤 API - Creating VNPay payment:', requestData)

    const response = await apiClient.post('/payments/vnpay/create', requestData)
    
    console.log('📥 API - VNPay payment response:', response)
    
    return response
  } catch (error) {
    console.error('❌ API - VNPay payment error:', error)
    throw error
  }
}
```

---

## 🎨 Component Vue

### File: `components/PaymentButton.vue`

```vue
<template>
  <div class="payment-button">
    <button 
      @click="handlePayment"
      :disabled="isLoading || !canPay"
      class="btn btn-primary"
    >
      <span v-if="isLoading">Đang xử lý...</span>
      <span v-else>Thanh toán VNPay</span>
    </button>

    <div v-if="error" class="error-message">
      {{ error }}
    </div>
  </div>
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
  },
  orderInfo: {
    type: String,
    default: null
  }
})

const paymentStore = usePaymentStore()

const isLoading = computed(() => paymentStore.isLoading)
const error = computed(() => paymentStore.error)
const canPay = computed(() => props.orderId > 0 && props.amount >= 1000)

const handlePayment = async () => {
  try {
    // Clear error trước
    paymentStore.clearPayment()

    // Validate
    if (!props.orderId || props.orderId <= 0) {
      throw new Error('OrderId không hợp lệ')
    }
    if (!props.amount || props.amount < 1000) {
      throw new Error('Số tiền phải lớn hơn hoặc bằng 1000 VND')
    }

    // Tạo payment request
    const paymentUrl = await paymentStore.createVNPayPayment({
      orderId: props.orderId,
      amount: props.amount,
      orderInfo: props.orderInfo || `Thanh toán đơn hàng #${props.orderId}`,
      purpose: 'ORDER_PAYMENT'
    })

    // Redirect đến VNPay
    paymentStore.redirectToVNPay(paymentUrl, props.orderId)

  } catch (error) {
    console.error('Payment error:', error)
    // Error đã được set trong store
  }
}
</script>

<style scoped>
.payment-button {
  margin: 1rem 0;
}

.error-message {
  color: red;
  margin-top: 0.5rem;
  font-size: 0.9rem;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
```

---

## 📄 Xử Lý Return URL

### File: `views/PaymentReturnPage.vue`

```vue
<template>
  <div class="payment-return-page">
    <div v-if="isProcessing" class="processing">
      <div class="spinner"></div>
      <p>Đang xử lý kết quả thanh toán...</p>
    </div>

    <div v-else-if="paymentResult" class="result">
      <div v-if="paymentResult.success" class="success">
        <div class="icon">✅</div>
        <h2>Thanh toán thành công!</h2>
        <p>Đơn hàng #{{ paymentResult.orderId }} đã được thanh toán thành công.</p>
        <button @click="goToOrderDetail" class="btn btn-primary">
          Xem chi tiết đơn hàng
        </button>
      </div>

      <div v-else class="failed">
        <div class="icon">❌</div>
        <h2>Thanh toán thất bại</h2>
        <p>{{ paymentResult.message }}</p>
        <p v-if="paymentResult.responseCode">
          Mã lỗi: {{ paymentResult.responseCode }}
        </p>
        <button @click="goToOrders" class="btn btn-secondary">
          Quay lại danh sách đơn hàng
        </button>
      </div>
    </div>

    <div v-else class="error">
      <p>Không thể xử lý kết quả thanh toán.</p>
      <button @click="goToOrders" class="btn btn-secondary">
        Quay lại danh sách đơn hàng
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { usePaymentStore } from '@/stores/paymentStore'

const route = useRoute()
const router = useRouter()
const paymentStore = usePaymentStore()

const isProcessing = ref(true)
const paymentResult = ref(null)

onMounted(async () => {
  try {
    // Lấy query params từ URL
    const queryParams = route.query

    console.log('📥 Payment return query params:', queryParams)

    // Xử lý kết quả thanh toán
    const result = paymentStore.handleVNPayReturn(queryParams)
    paymentResult.value = result

    // Clear sessionStorage
    sessionStorage.removeItem('vnpay_payment_order_id')
    sessionStorage.removeItem('vnpay_payment_timestamp')

    // Nếu thanh toán thành công, có thể gọi API để refresh order status
    if (result.success && result.orderId) {
      // TODO: Gọi API để refresh order status nếu cần
      // await orderStore.fetchOrder(result.orderId)
    }

  } catch (error) {
    console.error('Error processing payment return:', error)
    paymentResult.value = {
      success: false,
      message: 'Có lỗi xảy ra khi xử lý kết quả thanh toán'
    }
  } finally {
    isProcessing.value = false
  }
})

const goToOrderDetail = () => {
  if (paymentResult.value?.orderId) {
    router.push(`/orders/${paymentResult.value.orderId}`)
  } else {
    router.push('/orders')
  }
}

const goToOrders = () => {
  router.push('/orders')
}
</script>

<style scoped>
.payment-return-page {
  max-width: 600px;
  margin: 2rem auto;
  padding: 2rem;
  text-align: center;
}

.processing {
  padding: 2rem;
}

.spinner {
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  width: 40px;
  height: 40px;
  animation: spin 1s linear infinite;
  margin: 0 auto 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.result {
  padding: 2rem;
}

.success {
  color: #27ae60;
}

.failed {
  color: #e74c3c;
}

.icon {
  font-size: 4rem;
  margin-bottom: 1rem;
}

h2 {
  margin: 1rem 0;
}

.btn {
  margin-top: 1rem;
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
}

.btn-primary {
  background-color: #3498db;
  color: white;
}

.btn-secondary {
  background-color: #95a5a6;
  color: white;
}
</style>
```

---

## 🔧 Router Configuration

### File: `router/index.js`

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import PaymentReturnPage from '@/views/PaymentReturnPage.vue'

const routes = [
  // ... other routes
  
  {
    path: '/orders-page',
    name: 'OrdersPage',
    component: () => import('@/views/OrdersPage.vue'),
    // Xử lý query params paymentResult
    beforeEnter: (to, from, next) => {
      if (to.query.paymentResult === 'true') {
        // Redirect đến PaymentReturnPage để xử lý
        next({
          name: 'PaymentReturn',
          query: to.query
        })
      } else {
        next()
      }
    }
  },
  
  {
    path: '/payment/return',
    name: 'PaymentReturn',
    component: PaymentReturnPage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
```

---

## 📝 Sử Dụng Trong Component

### Ví dụ: `views/CheckoutPage.vue`

```vue
<template>
  <div class="checkout-page">
    <h1>Thanh toán</h1>
    
    <div class="order-summary">
      <p>Tổng tiền: {{ formatCurrency(totalAmount) }}</p>
    </div>

    <PaymentButton
      :order-id="orderId"
      :amount="totalAmount"
      :order-info="`Thanh toán đơn hàng #${orderId}`"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import PaymentButton from '@/components/PaymentButton.vue'

const orderId = ref(425) // Lấy từ route hoặc state
const totalAmount = ref(330000) // Tổng tiền đơn hàng

const formatCurrency = (amount) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(amount)
}
</script>
```

---

## 🛠️ Error Handling

### File: `utils/paymentErrorHandler.js`

```javascript
/**
 * Xử lý lỗi từ VNPay payment
 */
export const handleVNPayError = (error) => {
  console.error('VNPay Error:', error)

  // Lỗi từ API
  if (error.response) {
    const status = error.response.status
    const data = error.response.data

    switch (status) {
      case 400:
        return 'Dữ liệu thanh toán không hợp lệ. Vui lòng kiểm tra lại.'
      case 401:
        return 'Bạn chưa đăng nhập. Vui lòng đăng nhập để thanh toán.'
      case 403:
        return 'Bạn không có quyền thực hiện thanh toán này.'
      case 500:
        return 'Lỗi server. Vui lòng thử lại sau.'
      default:
        return data?.message || 'Có lỗi xảy ra khi tạo thanh toán.'
    }
  }

  // Lỗi network
  if (error.request) {
    return 'Không thể kết nối đến server. Vui lòng kiểm tra kết nối mạng.'
  }

  // Lỗi khác
  return error.message || 'Có lỗi xảy ra. Vui lòng thử lại.'
}
```

---

## 📋 Checklist Implementation

- [ ] Tạo `stores/paymentStore.js` với các actions cần thiết
- [ ] Tạo `services/paymentService.js` để gọi API
- [ ] Tạo `components/PaymentButton.vue` để hiển thị nút thanh toán
- [ ] Tạo `views/PaymentReturnPage.vue` để xử lý return URL
- [ ] Cấu hình router để xử lý `/orders-page?paymentResult=true&...`
- [ ] Tích hợp vào checkout/order page
- [ ] Test flow thanh toán đầy đủ
- [ ] Xử lý error cases

---

## 🧪 Test Flow

1. User click "Thanh toán VNPay"
2. Frontend gọi API → Backend trả về paymentUrl
3. Frontend redirect đến VNPay
4. User thanh toán trên VNPay
5. VNPay redirect về Frontend với query params
6. Frontend hiển thị kết quả thanh toán

---

## 📞 Support

Nếu gặp vấn đề:
- Kiểm tra console logs
- Kiểm tra Network tab trong DevTools
- Kiểm tra response từ API
- Kiểm tra query params từ return URL

