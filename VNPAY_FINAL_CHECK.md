# 🔥 VNPay Final Check - Kiểm Tra Cuối Cùng

## ✅ IPN URL Đã Được Đăng Ký

IPN URL của bạn: `https://undeputized-unreprehensibly-charlize.ngrok-free.dev/api/payments/vnpay/callback`

Đã được đăng ký tại: https://sandbox.vnpayment.vn/vnpaygw-sit-testing/ipn

---

## 🔍 KIỂM TRA QUAN TRỌNG

### 1. Ngrok Port - QUAN TRỌNG NHẤT!

**Kiểm tra ngrok đang forward đến port nào:**

Mở terminal ngrok và xem dòng "Forwarding":
- ❌ **SAI**: `-> http://localhost:3000` (Frontend port)
- ✅ **ĐÚNG**: `-> http://localhost:1234` (Backend port)

**Nếu ngrok đang forward đến port 3000:**

1. Dừng ngrok (Ctrl+C)
2. Chạy lại: `ngrok http 1234`
3. Copy URL mới từ ngrok
4. Cập nhật `.env`:
   ```env
   VNPAY_RETURN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/return
   VNPAY_IPN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/callback
   ```
5. Cập nhật IPN URL trong VNPay Merchant Portal
6. Restart backend

---

### 2. File .env Cần Có

```env
# VNPay Configuration (BẮT BUỘC)
VNPAY_TMN_CODE=PHIUAYRK
VNPAY_HASH_SECRET=G392ATCKG5Z5M61W43ZYSDL381M3MJ59

# VNPay URLs (BẮT BUỘC - dùng ngrok URL)
VNPAY_RETURN_URL=https://undeputized-unreprehensibly-charlize.ngrok-free.dev/api/payments/vnpay/return
VNPAY_IPN_URL=https://undeputized-unreprehensibly-charlize.ngrok-free.dev/api/payments/vnpay/callback
```

---

### 3. Các Fix Đã Áp Dụng

1. ✅ **Hash query string KHÔNG encode** - Đã fix
2. ✅ **Encode params khi tạo URL cuối cùng** - Đã fix
3. ✅ **Lấy IP thật từ request** (fallback: 8.8.8.8) - Đã fix
4. ✅ **Đổi vnp_OrderType sang "billpayment"** - Đã fix
5. ✅ **Xử lý orderInfo (bỏ dấu tiếng Việt)** - Đã fix

---

### 4. Kiểm Tra Logs Sau Khi Test

Sau khi gọi API tạo payment, kiểm tra logs:

**Query string (KHÔNG encode - để hash):**
- Phải KHÔNG có `%20`, `%3A`, `%2F` (không encode)
- Ví dụ: `vnp_Amount=33000000&vnp_Command=pay&vnp_CreateDate=20251221122239&...`

**SecureHash:**
- Phải có 128 ký tự (HMAC SHA512)
- Ví dụ: `eaff73bf00d0498055afc869fe4c21a9ba0a7ec18b6eb471b815a9f289c65816a2c96a6eb87377be2b52ad7a396dbc0ffe505240b3157b9f4b8d22584938ef4f`

**URL cuối cùng:**
- Phải có params đã encode (có `%20`, `%3A`, `%2F`)
- Ví dụ: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?vnp_Amount=33000000&vnp_Command=pay&...`

---

### 5. Test Flow

1. ✅ Đảm bảo ngrok đang chạy với port 1234
2. ✅ Đảm bảo backend đang chạy ở port 1234
3. ✅ Đảm bảo file `.env` có đầy đủ biến
4. ✅ Restart backend
5. ✅ Gọi API tạo payment
6. ✅ Kiểm tra logs
7. ✅ Redirect đến VNPay và xem có còn lỗi không

---

## 🐛 Nếu Vẫn Lỗi Code 99

Có thể thử:

1. **Kiểm tra lại TMN_CODE và HASH_SECRET:**
   - Đảm bảo cùng môi trường (Sandbox)
   - Kiểm tra trong VNPay Merchant Portal

2. **Thử cách hash khác:**
   - Code đã log cả HMAC SHA512 và SHA512 direct
   - Có thể thử đổi sang SHA512 direct nếu cần

3. **Kiểm tra ngrok:**
   - Đảm bảo ngrok đang forward đến port 1234
   - Đảm bảo ngrok đang chạy khi test

4. **Liên hệ VNPay:**
   - Email: hotrovnpay@vnpay.vn
   - Hotline: 1900 55 55 77
   - Cung cấp: Mã tra cứu từ lỗi, logs từ backend

