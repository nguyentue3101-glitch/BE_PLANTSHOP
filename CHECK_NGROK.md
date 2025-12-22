# 🔥 KIỂM TRA NGROK - QUAN TRỌNG!

## ❌ VẤN ĐỀ PHÁT HIỆN TỪ LOGS

Từ logs, tôi thấy URL đã đúng:
- ✅ Return URL: `https://undeputized-unreprehensibly-charlize.ngrok-free.dev/api/payments/vnpay/return`
- ✅ IPN URL: `https://undeputized-unreprehensibly-charlize.ngrok-free.dev/api/payments/vnpay/callback`

**NHƯNG** từ hình ảnh ngrok trước đó, ngrok đang forward đến:
```
Forwarding: ... -> http://localhost:3000
```

## 🔥 CÁCH KIỂM TRA VÀ SỬA

### Bước 1: Kiểm tra ngrok đang forward đến port nào

Mở terminal ngrok và xem dòng "Forwarding":
- ❌ **SAI**: `-> http://localhost:3000` (Frontend port)
- ✅ **ĐÚNG**: `-> http://localhost:1234` (Backend port)

### Bước 2: Nếu ngrok đang forward đến port 3000

1. **Dừng ngrok** (Ctrl+C trong terminal ngrok)

2. **Chạy lại với port đúng:**
   ```bash
   ngrok http 1234
   ```

3. **Copy URL mới** từ ngrok (có thể khác URL cũ)

4. **Cập nhật file `.env`:**
   ```env
   VNPAY_RETURN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/return
   VNPAY_IPN_URL=https://new-ngrok-url.ngrok-free.dev/api/payments/vnpay/callback
   ```

5. **Cập nhật IPN URL trong VNPay Merchant Portal:**
   - Vào: https://sandbox.vnpayment.vn/merchantv2/
   - Cập nhật IPN URL với URL mới từ ngrok

6. **Restart backend**

### Bước 3: Test lại

Sau khi sửa, test lại và kiểm tra logs xem:
- ✅ Return URL và IPN URL có đúng không
- ✅ Query string có dùng `%20` thay vì `+` không
- ✅ SecureHash có được tạo đúng không

---

## 📋 CÁC FIX ĐÃ ÁP DỤNG

1. ✅ **Thay `+` thành `%20` trong query string khi hash** - Đã fix
2. ✅ **URL encode params trước khi hash** - Đã fix
3. ✅ **Lấy IP thật từ request** - Đã fix
4. ✅ **Đổi vnp_OrderType sang "billpayment"** - Đã fix
5. ✅ **Xử lý orderInfo (bỏ dấu tiếng Việt)** - Đã fix

---

## ⚠️ LƯU Ý

- Ngrok URL có thể thay đổi mỗi lần restart ngrok
- Đảm bảo ngrok đang chạy khi test
- Đảm bảo backend đang chạy ở port 1234
- Đảm bảo VNPay Merchant Portal đã cập nhật IPN URL

