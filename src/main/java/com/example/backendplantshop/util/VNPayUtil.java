// package com.example.backendplantshop.util;

// import javax.crypto.Mac;
// import javax.crypto.spec.SecretKeySpec;
// import java.nio.charset.StandardCharsets;
// import java.security.InvalidKeyException;
// import java.security.MessageDigest;
// import java.security.NoSuchAlgorithmException;
// import java.util.Map;
// import java.util.TreeMap;

// public class VNPayUtil {
    
//     private static final String HMAC_SHA512 = "HmacSHA512";
//     private static final String SHA512 = "SHA-512";
    
//     /**
//      * Tạo chữ ký số (signature) cho VNPay Payment
//      * 🔥 VNPay yêu cầu: Hash (queryString + secretKey) bằng HMAC SHA512 với secretKey làm key
//      * Hoặc có thể là: Hash (queryString + secretKey) bằng SHA512 trực tiếp
//      * 
//      * Cách 1: HMAC SHA512(queryString + secretKey) với secretKey làm key
//      * Cách 2: SHA512(queryString + secretKey) trực tiếp
//      */
//     public static String createSignature(String secretKey, String queryString) {
//         if (secretKey == null || secretKey.trim().isEmpty()) {
//             throw new IllegalArgumentException("VNPay HashSecret không được để trống");
//         }
//         if (queryString == null || queryString.trim().isEmpty()) {
//             throw new IllegalArgumentException("Query string không được để trống");
//         }
        
//         try {
//             // 🔥 CÁCH 1: HMAC SHA512 của (queryString + secretKey) với secretKey làm key
//             // Đây là cách phổ biến nhất theo tài liệu VNPay
//             String hashData = queryString + secretKey;
//             Mac mac = Mac.getInstance(HMAC_SHA512);
//             SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
//             mac.init(secretKeySpec);
            
//             byte[] hashBytes = mac.doFinal(hashData.getBytes(StandardCharsets.UTF_8));
//             return bytesToHex(hashBytes);
//         } catch (NoSuchAlgorithmException | InvalidKeyException e) {
//             throw new RuntimeException("Lỗi khi tạo signature VNPay: " + e.getMessage(), e);
//         }
//     }
    
//     /**
//      * Tạo chữ ký số (signature) - Cách khác: HMAC SHA512 của queryString với secretKey làm key
//      * (Không thêm secretKey vào queryString)
//      */
//     public static String createSignatureMethod2(String secretKey, String queryString) {
//         if (secretKey == null || secretKey.trim().isEmpty()) {
//             throw new IllegalArgumentException("VNPay HashSecret không được để trống");
//         }
//         if (queryString == null || queryString.trim().isEmpty()) {
//             throw new IllegalArgumentException("Query string không được để trống");
//         }
        
//         try {
//             // CÁCH 2: HMAC SHA512 của queryString với secretKey làm key (KHÔNG thêm secretKey vào queryString)
//             Mac mac = Mac.getInstance(HMAC_SHA512);
//             SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
//             mac.init(secretKeySpec);
            
//             byte[] hashBytes = mac.doFinal(queryString.getBytes(StandardCharsets.UTF_8));
//             return bytesToHex(hashBytes);
//         } catch (NoSuchAlgorithmException | InvalidKeyException e) {
//             throw new RuntimeException("Lỗi khi tạo signature VNPay: " + e.getMessage(), e);
//         }
//     }
    
//     /**
//      * Tạo chữ ký số (signature) cho VNPay Payment - Cách 2: Hash (queryString + secretKey) bằng SHA512
//      * Thử cách này nếu HMAC SHA512 không hoạt động
//      */
//     public static String createSignatureSHA512(String secretKey, String queryString) {
//         if (secretKey == null || secretKey.trim().isEmpty()) {
//             throw new IllegalArgumentException("VNPay HashSecret không được để trống");
//         }
//         if (queryString == null || queryString.trim().isEmpty()) {
//             throw new IllegalArgumentException("Query string không được để trống");
//         }
        
//         try {
//             // Hash (queryString + secretKey) bằng SHA512
//             String hashData = queryString + secretKey;
//             MessageDigest digest = MessageDigest.getInstance(SHA512);
//             byte[] hashBytes = digest.digest(hashData.getBytes(StandardCharsets.UTF_8));
//             return bytesToHex(hashBytes);
//         } catch (NoSuchAlgorithmException e) {
//             throw new RuntimeException("Lỗi khi tạo signature VNPay: " + e.getMessage(), e);
//         }
//     }
    
//     /**
//      * Chuyển đổi byte array sang hex string
//      */
//     private static String bytesToHex(byte[] bytes) {
//         StringBuilder result = new StringBuilder();
//         for (byte b : bytes) {
//             result.append(String.format("%02x", b));
//         }
//         return result.toString();
//     }
    
//     /**
//      * Tạo query string từ Map các tham số, sắp xếp theo thứ tự alphabet
//      * Loại bỏ các tham số null hoặc rỗng
//      * 🔥 QUAN TRỌNG: VNPay yêu cầu hash query string KHÔNG encode, sau đó mới encode khi tạo URL
//      */
//     public static String createQueryString(Map<String, String> params) {
//         return createQueryString(params, false);
//     }
    
//     /**
//      * Tạo query string từ Map các tham số, sắp xếp theo thứ tự alphabet
//      * @param params Map chứa các tham số
//      * @param encode true nếu muốn URL encode, false nếu không encode (dùng để hash)
//      */
//     public static String createQueryString(Map<String, String> params, boolean encode) {
//         // Sử dụng TreeMap để tự động sắp xếp theo key (alphabet)
//         TreeMap<String, String> sortedParams = new TreeMap<>();
        
//         for (Map.Entry<String, String> entry : params.entrySet()) {
//             String key = entry.getKey();
//             String value = entry.getValue();
            
//             // Bỏ qua các tham số null, rỗng, hoặc là vnp_SecureHash
//             if (value != null && !value.trim().isEmpty() && !key.equals("vnp_SecureHash")) {
//                 sortedParams.put(key, value);
//             }
//         }
        
//         // Tạo query string: key1=value1&key2=value2&...
//         // 🔥 THEO CODE DEMO VNPAY (ajaxServlet.java):
//         // - Khi hash: chỉ encode VALUE bằng US_ASCII, KEY không encode
//         // - Khi tạo URL: encode cả KEY và VALUE bằng UTF-8
//         StringBuilder queryString = new StringBuilder();
//         boolean first = true;
//         for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
//             if (!first) {
//                 queryString.append("&");
//             }
            
//             if (encode) {
//                 // URL encode key và value khi tạo URL cuối cùng (UTF-8)
//                 String encodedKey = java.net.URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
//                 String encodedValue = java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
//                 queryString.append(encodedKey).append("=").append(encodedValue);
//             } else {
//                 // 🔥 KHI HASH: Chỉ encode VALUE bằng US_ASCII (theo code demo VNPay)
//                 // KEY không encode, VALUE encode bằng US_ASCII
//                 String key = entry.getKey();
//                 String value = java.net.URLEncoder.encode(entry.getValue(), StandardCharsets.US_ASCII);
//                 queryString.append(key).append("=").append(value);
//             }
//             first = false;
//         }
        
//         return queryString.toString();
//     }
    
//     /**
//      * Verify signature từ callback VNPay
//      * VNPay gửi callback với tất cả params bao gồm cả vnp_SecureHash
//      * Cần loại bỏ vnp_SecureHash trước khi verify
//      * 
//      * 🔥 LƯU Ý: Phải dùng cùng cách hash như khi tạo payment request
//      */
//     public static boolean verifySignature(String signature, Map<String, String> params, String secretKey) {
//         try {
//             // Tạo query string từ params (đã loại bỏ vnp_SecureHash trong createQueryString)
//             String queryString = createQueryString(params);
//             // Hash: queryString + secretKey (được xử lý trong createSignature - Cách 1)
//             String expectedSignature = createSignature(secretKey, queryString);
//             return expectedSignature.equalsIgnoreCase(signature);
//         } catch (Exception e) {
//             return false;
//         }
//     }
    
//     /**
//      * Verify signature - Cách 2 (nếu dùng cách 2 để tạo hash)
//      */
//     public static boolean verifySignatureMethod2(String signature, Map<String, String> params, String secretKey) {
//         try {
//             String queryString = createQueryString(params);
//             String expectedSignature = createSignatureMethod2(secretKey, queryString);
//             return expectedSignature.equalsIgnoreCase(signature);
//         } catch (Exception e) {
//             return false;
//         }
//     }
// }

package com.example.backendplantshop.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public class VNPayUtil {

    private static final String HMAC_SHA512 = "HmacSHA512";

    /**
     * Tạo chữ ký HMAC SHA512 theo đúng chuẩn VNPay
     * vnp_SecureHash = HMAC_SHA512(secretKey, hashData)
     */
    public static String hmacSHA512(String secretKey, String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            mac.init(secretKeySpec);

            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
            return hash.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error while hashing VNPay data", e);
        }
    }

    /**
     * Tạo hashData (dùng để ký)
     * - Sắp xếp key theo alphabet
     * - KHÔNG encode key
     * - VALUE encode UTF-8
     * - Bỏ vnp_SecureHash nếu có
     */
    public static String buildHashData(Map<String, String> params) {
        TreeMap<String, String> sortedParams = new TreeMap<>();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getValue() != null
                    && !entry.getValue().isEmpty()
                    && !"vnp_SecureHash".equals(entry.getKey())) {
                sortedParams.put(entry.getKey(), entry.getValue());
            }
        }

        StringBuilder hashData = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (!first) {
                hashData.append("&");
            }
            hashData.append(entry.getKey())
                    .append("=")
                    .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            first = false;
        }

        return hashData.toString();
    }

    /**
     * Tạo query string cho URL redirect VNPay
     * - Encode cả KEY và VALUE (UTF-8)
     */
    public static String buildQueryString(Map<String, String> params) {
        TreeMap<String, String> sortedParams = new TreeMap<>(params);

        StringBuilder query = new StringBuilder();
        boolean first = true;

        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            if (entry.getValue() != null && !entry.getValue().isEmpty()) {
                if (!first) {
                    query.append("&");
                }
                query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                     .append("=")
                     .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
        }

        return query.toString();
    }

    /**
     * Verify chữ ký từ VNPay callback / return
     */
    public static boolean verifySignature(
            Map<String, String> params,
            String receivedSecureHash,
            String secretKey
    ) {
        String hashData = buildHashData(params);
        String expectedHash = hmacSHA512(secretKey, hashData);
        return expectedHash.equalsIgnoreCase(receivedSecureHash);
    }
}
