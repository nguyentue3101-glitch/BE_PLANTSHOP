# 🔥 Phân Tích Code Demo Chính Thức VNPay

## 📋 Cách Tạo Hash Signature Đúng (Từ Code Demo)

### File: `ajaxServlet.java` (dòng 78-102)

```java
// 1. Sắp xếp fieldNames theo alphabet
List fieldNames = new ArrayList(vnp_Params.keySet());
Collections.sort(fieldNames);

// 2. Tạo hashData và query
StringBuilder hashData = new StringBuilder();
StringBuilder query = new StringBuilder();
Iterator itr = fieldNames.iterator();
while (itr.hasNext()) {
    String fieldName = (String) itr.next();
    String fieldValue = (String) vnp_Params.get(fieldName);
    if ((fieldValue != null) && (fieldValue.length() > 0)) {
        //Build hash data - CHỈ ENCODE VALUE bằng US_ASCII
        hashData.append(fieldName);  // KEY không encode
        hashData.append('=');
        hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString())); // VALUE encode US_ASCII
        
        //Build query cho URL - ENCODE CẢ KEY VÀ VALUE bằng US_ASCII
        query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
        query.append('=');
        query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
        
        if (itr.hasNext()) {
            query.append('&');
            hashData.append('&');
        }
    }
}

// 3. Hash hashData với secretKey
String vnp_SecureHash = Config.hmacSHA512(Config.secretKey, hashData.toString());

// 4. Tạo URL cuối cùng
String queryUrl = query.toString();
queryUrl += "&vnp_SecureHash=" + vnp_SecureHash;
String paymentUrl = Config.vnp_PayUrl + "?" + queryUrl;
```

### File: `Config.java` - Method `hmacSHA512()`

```java
public static String hmacSHA512(final String key, final String data) {
    final Mac hmac512 = Mac.getInstance("HmacSHA512");
    byte[] hmacKeyBytes = key.getBytes();
    final SecretKeySpec secretKey = new SecretKeySpec(hmacKeyBytes, "HmacSHA512");
    hmac512.init(secretKey);
    byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
    byte[] result = hmac512.doFinal(dataBytes);
    // Convert to hex
    return hexString;
}
```

## 🔥 Điểm Quan Trọng

1. **Hash Data**: 
   - KEY không encode
   - VALUE encode bằng `US_ASCII` (không phải UTF-8)
   - Format: `fieldName=encodedValue&fieldName2=encodedValue2&...`

2. **Query URL**:
   - Cả KEY và VALUE đều encode bằng `US_ASCII`
   - Format: `encodedFieldName=encodedValue&encodedFieldName2=encodedValue2&...`

3. **Hash Algorithm**:
   - HMAC SHA512
   - Hash `hashData` với `secretKey` làm key
   - Data bytes: `data.getBytes(StandardCharsets.UTF_8)`

4. **Thứ tự**:
   - Sắp xếp fieldNames theo alphabet
   - Loại bỏ các field null hoặc rỗng
   - Loại bỏ `vnp_SecureHash` khi hash

## ✅ Code Đã Sửa

Đã cập nhật `VNPayUtil.createQueryString()` để:
- Khi hash (`encode=false`): Chỉ encode VALUE bằng US_ASCII, KEY không encode
- Khi tạo URL (`encode=true`): Encode cả KEY và VALUE bằng UTF-8

