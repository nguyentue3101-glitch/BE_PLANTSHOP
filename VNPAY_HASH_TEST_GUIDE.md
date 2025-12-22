# 🔥 VNPay Hash Test Guide - Tìm Cách Hash Đúng

## 📋 Vấn Đề

Lỗi code 99 từ VNPay thường do **chữ ký (vnp_SecureHash) sai**.

Backend hiện tại đang thử **3 cách hash khác nhau** và log ra để bạn so sánh.

---

## 🔍 3 Cách Hash Đang Được Test

### Cách 1: HMAC SHA512(queryString + secretKey) với secretKey làm key
```java
hashData = queryString + secretKey
HMAC SHA512(hashData) với secretKey làm key
```
**Đang được sử dụng mặc định**

### Cách 2: HMAC SHA512(queryString) với secretKey làm key
```java
HMAC SHA512(queryString) với secretKey làm key
(KHÔNG thêm secretKey vào queryString)
```

### Cách 3: SHA512(queryString + secretKey) trực tiếp
```java
hashData = queryString + secretKey
SHA512(hashData) trực tiếp (KHÔNG dùng HMAC)
```

---

## 🧪 Cách Test

### Bước 1: Gọi API tạo payment

Gọi API: `POST /api/payments/vnpay/create`

### Bước 2: Kiểm tra logs backend

Sau khi gọi API, kiểm tra logs backend. Bạn sẽ thấy:

```
Query string (KHÔNG encode - để hash): vnp_Amount=33000000&vnp_Command=pay&...
SecureHash (Cách 1 - HMAC SHA512(queryString+secretKey)): abc123...
SecureHash (Cách 2 - HMAC SHA512(queryString)): def456...
SecureHash (Cách 3 - SHA512(queryString+secretKey)): ghi789...
```

### Bước 3: Copy URL từ response

Copy `payUrl` từ response và mở trong browser.

### Bước 4: Kiểm tra kết quả

- ✅ **Nếu thành công**: Cách 1 đúng
- ❌ **Nếu vẫn lỗi code 99**: Thử đổi sang cách 2 hoặc cách 3

---

## 🔧 Cách Đổi Hash Method

Nếu cách 1 không hoạt động, sửa trong `VNPayServiceImpl.java`:

### Đổi sang Cách 2:
```java
// Dòng 156, đổi từ:
String vnpSecureHash = vnpSecureHash1;
// Thành:
String vnpSecureHash = vnpSecureHash2;
```

### Đổi sang Cách 3:
```java
// Dòng 156, đổi từ:
String vnpSecureHash = vnpSecureHash1;
// Thành:
String vnpSecureHash = vnpSecureHash3;
```

---

## 📝 Checklist Kiểm Tra

- [ ] Query string KHÔNG encode (không có %20, %3A, %2F)
- [ ] Query string đã sắp xếp theo alphabet
- [ ] Query string KHÔNG bao gồm vnp_SecureHash
- [ ] vnp_Amount đã nhân 100 (330000 → 33000000)
- [ ] vnp_TxnRef format đúng (orderId_timestamp)
- [ ] vnp_CreateDate format đúng (yyyyMMddHHmmss)
- [ ] SecureHash có 128 ký tự (HMAC SHA512)
- [ ] TMN_CODE và HASH_SECRET đúng và cùng môi trường

---

## 🐛 Debug Tips

1. **Copy query string từ logs** và test với tool VNPay (nếu có)
2. **So sánh 3 SecureHash** trong logs để xem có khác nhau không
3. **Kiểm tra URL cuối cùng** có đầy đủ params không
4. **Kiểm tra ngrok** đang forward đến port 1234

---

## 📞 Nếu Vẫn Lỗi

1. Gửi logs backend (query string và 3 SecureHash)
2. Gửi URL được redirect (từ frontend logs)
3. Kiểm tra VNPay Merchant Portal xem có cấu hình đúng không

