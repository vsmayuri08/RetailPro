package com.retailsystem.service;

import io.jsonwebtoken.io.Decoders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * Signs invoice numbers so a customer QR can open a digital receipt
 * without a login, while guessing invoice numbers alone is not enough.
 */
@Service
public class ReceiptTokenService {

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    public String sign(String invoiceNumber) {
        try {
            byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(invoiceNumber.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Could not sign receipt token", ex);
        }
    }

    public boolean matches(String invoiceNumber, String token) {
        if (invoiceNumber == null || token == null || token.isBlank()) {
            return false;
        }
        String expected = sign(invoiceNumber);
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                token.trim().toLowerCase().getBytes(StandardCharsets.UTF_8));
    }
}
