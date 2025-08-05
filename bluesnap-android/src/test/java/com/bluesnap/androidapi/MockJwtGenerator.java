package com.bluesnap.androidapi;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class MockJwtGenerator {

    public static String generateMockJwt(boolean isSandbox) {
        // Header
        Map<String, String> header = new HashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");
        String encodedHeader = base64UrlEncode(toJson(header));

        // Payload
        Map<String, Object> payload = new HashMap<>();
        payload.put("sub", "MOCK_SUB");
        payload.put("role", "MOCK_ROLE");
        payload.put("iat", System.currentTimeMillis() / 1000); // Issued at (current time in seconds)
        payload.put("exp", (System.currentTimeMillis() / 1000) + 3600); // Expires in 1 hour
        String encodedPayload = base64UrlEncode(toJson(payload));

        // Mock Signature
        String signature = "mock_signature"; // Replace with actual signing in real use cases

        // Construct the token
        String token = encodedHeader + "." + encodedPayload + "." + signature;

        // Ensure it ends with '_'
        if (isSandbox) {
            token += "_"; // Sandbox tokens end with '_'
        } else {
            token += new Random().nextBoolean() ? "1" : "2"; // Production tokens end with '1' or '2'
        }
        return token;
    }

    private static String base64UrlEncode(String input) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes());
    }

    private static String toJson(Map<?, ?> map) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                json.append("\"").append(entry.getValue()).append("\",");
            } else {
                json.append(entry.getValue()).append(",");
            }
        }
        // Remove trailing comma and close JSON
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }


}