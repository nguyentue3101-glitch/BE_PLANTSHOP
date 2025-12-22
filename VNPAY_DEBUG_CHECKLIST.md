# VNPay Debug Checklist

## Các điểm cần kiểm tra khi gặp lỗi "Có lỗi xảy ra trong quá trình xử lý"

### 1. Kiểm tra Logs
Sau khi gọi API tạo payment, kiểm tra logs để xem:
- ✅ Query string trước khi hash
- ✅ SecureHash được tạo ra
- ✅ Tất cả params được gửi đi
- ✅ URL cuối cùng (preview)

### 2. Kiểm tra Tham số Bắt buộc
Đảm bảo các tham số sau được gửi đúng:
- ✅ `vnp_Version`: "2.1.0"
- ✅ `vnp_Command`: "pay"
- ✅ `vnp_TmnCode`: Terminal Code (PHIUAYRK)
- ✅ `vnp_Amount`: Số tiền nhân 100 (đơn vị: xu)
- ✅ `vnp_CurrCode`: "VND"
- ✅ `vnp_TxnRef`: Tối đa 40 ký tự, chỉ chữ số và chữ cái
- ✅ `vnp_OrderInfo`: Tối đa 255 ký tự, KHÔNG có dấu tiếng Việt
- ✅ `vnp_OrderType`: "other"
- ✅ `vnp_Locale`: "vn" hoặc "en"
- ✅ `vnp_ReturnUrl`: URL công khai (KHÔNG dùng localhost)
- ✅ `vnp_IpAddr`: IP của khách hàng
- ✅ `vnp_CreateDate`: Format yyyyMMddHHmmss
- ✅ `vnp_SecureHash`: Hash được tạo từ query string

### 3. Kiểm tra URL
- ❌ **KHÔNG** sử dụng `localhost` cho Return URL và IPN URL
- ✅ Sử dụng ngrok hoặc public URL để test
- ✅ URL phải có thể truy cập từ internet

### 4. Kiểm tra Hash/Signature
VNPay sử dụng **HMAC SHA512** với:
- Query string: Tất cả params sắp xếp theo alphabet (không bao gồm vnp_SecureHash)
- Secret key: HashSecret từ VNPay
- Hash: HMAC SHA512 của query string với secret key làm key

### 5. Kiểm tra Format
- `vnp_TxnRef`: Chỉ chữ số, chữ cái, dấu gạch dưới. Tối đa 40 ký tự
- `vnp_OrderInfo`: Không có dấu tiếng Việt, không có ký tự đặc biệt (#, @, etc.)
- `vnp_Amount`: Phải là số nguyên (đã nhân 100)
- `vnp_CreateDate`: Format yyyyMMddHHmmss (ví dụ: 20251221100000)

### 6. Các Lỗi Thường Gặp

#### Lỗi "Sai chữ ký"
- Kiểm tra HashSecret có đúng không
- Kiểm tra query string có đúng thứ tự alphabet không
- Kiểm tra có bỏ vnp_SecureHash khi tạo query string không

#### Lỗi "Có lỗi xảy ra trong quá trình xử lý"
- Kiểm tra Return URL và IPN URL có thể truy cập được không
- Kiểm tra format các tham số
- Kiểm tra thiếu tham số bắt buộc
- Kiểm tra orderInfo có dấu tiếng Việt không

### 7. Cách Test với Ngrok

1. Cài đặt ngrok:
```bash
# Download từ https://ngrok.com/
ngrok http 1234
```

2. Lấy URL từ ngrok (ví dụ: `https://abc123.ngrok.io`)

3. Cập nhật `.env`:
```env
VNPAY_RETURN_URL=https://abc123.ngrok.io/api/payments/vnpay/return
VNPAY_IPN_URL=https://abc123.ngrok.io/api/payments/vnpay/callback
```

4. Restart backend và test lại

### 8. Mã Lỗi VNPay
Tham khảo bảng mã lỗi tại: https://sandbox.vnpayment.vn/apis/docs/bang-ma-loi/

### 9. Liên Hệ Hỗ Trợ
- Email: hotrovnpay@vnpay.vn
- Hotline: 1900 55 55 77

