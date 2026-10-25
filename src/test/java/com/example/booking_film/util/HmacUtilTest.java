package com.example.booking_film.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HmacUtilTest {

    @Test
    @DisplayName("HmacSHA512 phải tạo chuỗi hash 128 ký tự hex hợp lệ")
    void testHmacSHA512_Success() {
        String key = "my-secret-key";
        String data = "booking-data-12345";

        String hash = HmacUtil.hmacSHA512(key, data);

        assertNotNull(hash);
        assertEquals(128, hash.length(), "HMAC-SHA512 phải có độ dài 128 ký tự hex");
    }

    @Test
    @DisplayName("HmacSHA512 phải trả về cùng kết quả cho cùng key và data")
    void testHmacSHA512_Deterministic() {
        String key = "secure-key";
        String data = "transaction-info";

        String hash1 = HmacUtil.hmacSHA512(key, data);
        String hash2 = HmacUtil.hmacSHA512(key, data);

        assertEquals(hash1, hash2);
    }

    @Test
    @DisplayName("HmacSHA512 phải trả về kết quả khác nhau khi key khác nhau")
    void testHmacSHA512_DifferentKeys() {
        String data = "same-data";
        String hash1 = HmacUtil.hmacSHA512("key-1", data);
        String hash2 = HmacUtil.hmacSHA512("key-2", data);

        assertNotEquals(hash1, hash2);
    }
}
