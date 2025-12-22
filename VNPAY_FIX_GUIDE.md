# 🔥 VNPay Fix Guide - Các Lỗi Đã Phát Hiện

## ❌ VẤN ĐỀ CHÍNH: Ngrok Forward SAI PORT

Từ hình ảnh ngrok của bạn:
```
Forwarding: https://undeputized-unreprehensibly-charlize.ngrok-free.dev -> http://localhost:3000
```

**❌ SAI:** Ngrok đang forward đến port **3000** (Frontend)
**✅ ĐÚNG:** Phải forward đến port **1234** (Backend)

### Cách sửa:

1. **Dừng ngrok hiện tại** (Ctrl+C)

2. **Chạy lại ngrok với port đúng:**
```bash
ngrok http 1234
```

3. **Copy URL mới từ ngrok** (có thể khác URL cũ)

4. **Cập nhật file .env:**
```env
VNPAY_RETURN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/return
VNPAY_IPN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/callback
```

5. **Cập nhật trong VNPay Merchant Portal:**
   - Vào: https://sandbox.vnpayment.vn/merchantv2/
   - Cập nhật IPN URL với URL mới từ ngrok

---

## ⚠️ VẤN ĐỀ 2: File .env Thiếu Biến

Từ `application.yml`, các biến sau **BẮT BUỘC** phải có trong `.env`:

```env
# VNPay Configuration (BẮT BUỘC)
VNPAY_TMN_CODE=PHIUAYRK
VNPAY_HASH_SECRET=G392ATCKG5Z5M61W43ZYSDL381M3MJ59

# VNPay URLs (BẮT BUỘC nếu dùng ngrok)
VNPAY_RETURN_URL=https://your-ngrok-url.ngrok-free.dev/api/payments/vnpay/return
VNPAY_IPN_URL=https://your-ngrok-url.ngrok-free.dev/api/payments/vnpay/callback
```

**Kiểm tra:** Đảm bảo file `.env` có đầy đủ các biến trên.

---

## ✅ Các Fix Đã Áp Dụng Trong Code

1. ✅ **URL encode params trước khi hash** - Đã fix trong `VNPayUtil.createQueryString()`
2. ✅ **Lấy IP thật từ request** - Đã fix trong `VNPayServiceImpl.getClientIpAddress()`
3. ✅ **Đổi vnp_OrderType từ "other" sang "billpayment"** - Đã fix
4. ✅ **Xử lý orderInfo (bỏ dấu tiếng Việt)** - Đã fix

---

## 📋 CHECKLIST ĐỂ FIX LỖI

- [ ] **Ngrok đang chạy với port 1234** (không phải 3000)
- [ ] **File .env có đầy đủ biến:**
  - [ ] `VNPAY_TMN_CODE`
  - [ ] `VNPAY_HASH_SECRET`
  - [ ] `VNPAY_RETURN_URL` (với ngrok URL)
  - [ ] `VNPAY_IPN_URL` (với ngrok URL)
- [ ] **VNPay Merchant Portal đã cập nhật IPN URL** với ngrok URL mới
- [ ] **Backend đã restart** sau khi cập nhật .env
- [ ] **Backend đang chạy ở port 1234**

---

## 🧪 Test Sau Khi Fix

1. Gọi API tạo payment: `POST /api/payments/vnpay/create`
2. Kiểm tra logs xem:
   - ✅ Return URL và IPN URL có đúng ngrok URL không
   - ✅ Client IP có đúng không (không phải 127.0.0.1)
   - ✅ Query string và SecureHash có được tạo đúng không
3. Thử thanh toán và xem có còn lỗi không

---

## 📞 Nếu Vẫn Lỗi

Kiểm tra logs backend và gửi:
- Query string trước khi hash
- SecureHash được tạo
- Tất cả params được gửi đi
- Return URL và IPN URL

