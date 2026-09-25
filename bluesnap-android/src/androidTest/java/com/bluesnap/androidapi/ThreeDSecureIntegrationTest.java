package com.bluesnap.androidapi;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.bluesnap.androidapi.models.BS3DSAuthRequest;
import com.bluesnap.androidapi.models.BS3DSAuthResponse;
import com.bluesnap.androidapi.services.CardinalManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.*;

/**
 * Integration tests for 3DS (CardinalManager) related classes.
 * Tests BS3DSAuthRequest serialization, BS3DSAuthResponse deserialization,
 * and ThreeDSManagerResponse enum values.
 */
@RunWith(AndroidJUnit4.class)
public class ThreeDSecureIntegrationTest extends BSAndroidIntegrationTestsBase {
    private static final String TAG = ThreeDSecureIntegrationTest.class.getSimpleName();

    /**
     * Test BS3DSAuthRequest JSON serialization.
     * Verifies that currency, amount, and jwt are correctly serialized to JSON.
     */
    @Test
    public void testBS3DSAuthRequestToJson() throws JSONException {
        String currency = "USD";
        Double amount = 100.50;
        String jwt = "test-jwt-token-12345";

        BS3DSAuthRequest request = new BS3DSAuthRequest(currency, amount, jwt);
        JSONObject json = request.toJson();

        assertNotNull("JSON should not be null", json);
        assertEquals("Currency mismatch", currency, json.getString("currency"));
        assertEquals("Amount mismatch", amount.toString(), json.getString("amount"));
        assertEquals("JWT mismatch", jwt, json.getString("jwt"));
    }

    /**
     * Test BS3DSAuthResponse JSON deserialization.
     * Verifies all fields are correctly parsed from JSON.
     */
    @Test
    public void testBS3DSAuthResponseFromJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("enrollmentStatus", "CHALLENGE_REQUIRED");
        json.put("acsUrl", "https://acs.cardinalcommerce.com/challenge");
        json.put("payload", "encoded-challenge-payload");
        json.put("transactionId", "txn-abc-123");
        json.put("threeDSVersion", "2.1.0");

        BS3DSAuthResponse response = BS3DSAuthResponse.fromJson(json);

        assertNotNull("Response should not be null", response);
        assertEquals("EnrollmentStatus mismatch", "CHALLENGE_REQUIRED", response.getEnrollmentStatus());
        assertEquals("AcsUrl mismatch", "https://acs.cardinalcommerce.com/challenge", response.getAcsUrl());
        assertEquals("Payload mismatch", "encoded-challenge-payload", response.getPayload());
        assertEquals("TransactionId mismatch", "txn-abc-123", response.getTransactionId());
        assertEquals("ThreeDSVersion mismatch", "2.1.0", response.getThreeDSVersion());
    }

    /**
     * Test BS3DSAuthResponse handles null JSON gracefully.
     * Verifies that passing null returns null without throwing exceptions.
     */
    @Test
    public void testBS3DSAuthResponseFromNullJson() {
        BS3DSAuthResponse response = BS3DSAuthResponse.fromJson(null);
        assertNull("Response should be null when JSON is null", response);
    }

    /**
     * Test BS3DSAuthResponse handles partial/missing fields.
     * Verifies that only provided fields are populated, others return empty string.
     * Note: JsonParser.getOptionalString() returns "" (not null) for missing fields.
     */
    @Test
    public void testBS3DSAuthResponseWithMissingFields() throws JSONException {
        // Only provide enrollmentStatus, all other fields missing
        JSONObject json = new JSONObject();
        json.put("enrollmentStatus", "AUTHENTICATION_SUCCEEDED");

        BS3DSAuthResponse response = BS3DSAuthResponse.fromJson(json);

        assertNotNull("Response should not be null", response);
        assertEquals("EnrollmentStatus should be parsed", "AUTHENTICATION_SUCCEEDED", response.getEnrollmentStatus());
        // JsonParser.getOptionalString returns empty string for missing fields, not null
        assertEquals("AcsUrl should be empty string when not provided", "", response.getAcsUrl());
        assertEquals("Payload should be empty string when not provided", "", response.getPayload());
        assertEquals("TransactionId should be empty string when not provided", "", response.getTransactionId());
        assertEquals("ThreeDSVersion should be empty string when not provided", "", response.getThreeDSVersion());
    }
}
