package org.egov.eTreasury.service;

import org.egov.eTreasury.config.PaymentConfiguration;
import org.egov.eTreasury.util.ETreasuryUtil;
import org.egov.eTreasury.util.EncryptionUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.egov.eTreasury.model.*;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    private final PaymentConfiguration config;

    private final ETreasuryUtil treasuryUtil;

    private final ObjectMapper objectMapper;

    private final EncryptionUtil encryptionUtil;

    @Autowired
    public PaymentService(PaymentConfiguration config, ETreasuryUtil treasuryUtil,
                          ObjectMapper objectMapper, EncryptionUtil encryptionUtil) {
        this.config = config;
        this.treasuryUtil = treasuryUtil;
        this.objectMapper = objectMapper;
        this.encryptionUtil = encryptionUtil;
    }

    private Map<String, String> authenticate() {
        Map<String, String> secretMap = new HashMap<>();
        try {
            // Generate client secret and app key
            secretMap = encryptionUtil.getClientSecretAndAppKey(config.getClientSecret(), config.getPublicKey());

            // Prepare authentication request payload
            AuthRequest authRequest = new AuthRequest(secretMap.get("encodedAppKey"));
            String payload = objectMapper.writeValueAsString(authRequest);

            // Call the authentication service
            ResponseEntity<?> responseEntity = treasuryUtil.callAuthService(config.getClientId(), config.getClientSecret(),
                    payload, config.getAuthUrl());
            log.info("Response Body : {}", responseEntity.getBody());        
            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                AuthResponse response = objectMapper.convertValue(responseEntity.getBody(), AuthResponse.class);
                secretMap.put("sek", response.getData().getSek());
                secretMap.put("authToken", response.getData().getAuthToken());
            } else {
                throw new CustomException("AUTHENTICATION_FAILED", "Authentication request failed with status: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Authentication process failed: ", e);
            throw new CustomException("AUTHENTICATION_ERROR", "Error occurred during authentication");
        }
        return secretMap;
    }

    public String processPayment(PaymentDetails paymentDetails) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(paymentDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<?> responseEntity = callService(headersData, postBody, config.getChallanGenerateUrl());

            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return objectMapper.convertValue(responseEntity.getBody(), String.class);
            } else {
                throw new CustomException("CHALLAN_GENERATION_FAILED", "Error occurred for challan generate request");
            }
        } catch (Exception e) {
            log.error("Payment processing error: ", e);
            throw new CustomException("PAYMENT_PROCESSING_ERROR", "Error occurred during generation oF chsllan");
        }
    }

    public String doubleVerifyPayment(VerificationDetails verificationDetails) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(verificationDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<?> responseEntity = callService(headersData, postBody, config.getDoubleVerificationUrl());

            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return objectMapper.convertValue(responseEntity.getBody(), String.class);
            } else {
                throw new CustomException("DOUBLE_VERIFICATION_FAILED", "Double verification request Failed");
            }
        } catch (Exception e) {
            log.error("Double verification Error: ", e);
            throw new CustomException("DOUBLE_VERIFICATION_ERROR", "Error occurred during double verification");
        }
    }

    public ByteArrayResource printPayInSlip(PrintDetails printDetails) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(printDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<?> responseEntity = callService(headersData, postBody, config.getPrintSlipUrl());

            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return objectMapper.convertValue(responseEntity.getBody(), ByteArrayResource.class);
            } else {
                throw new CustomException("PRINT_SLIP_FAILED", "Pay in slip request failed");
            }
        } catch (Exception e) {
            log.error("Print slip generation Error: ", e);
            throw new CustomException("PRINT_SLIP_ERROR", "Error occurred during pay in slip generation");
        }
    }

    public TransactionDetails fetchTransactionDetails(TransactionDetails transactionDetails) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(transactionDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<?> responseEntity = callService(headersData, postBody, config.getTransactionDetailsUrl());

            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return objectMapper.convertValue(responseEntity.getBody(), TransactionDetails.class);
            } else {
                throw new CustomException("TRANSACTION_DETAILS_FAILED", "Transaction details request failed");
            }
        } catch (Exception e) {
            log.error("Transaction details retrieval failed: ", e);
            throw new CustomException("TRANSACTION_DETAILS_ERROR", "Error ccurred during transaction details retrieval");
        }
    }


    private ResponseEntity callService(String headersData, String postBody, String url) {
        return treasuryUtil.callService(headersData, postBody, url);
    }

    private String generatePostBody(String decryptedSek, String jsonData) {
        try {
            // Convert SEK to AES key
            SecretKey aesKey = new SecretKeySpec(decryptedSek.getBytes(StandardCharsets.UTF_8), "AES");

            // Initialize AES cipher in encryption mode
            Cipher aesCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);

            // Encrypt JSON data
            byte[] encryptedDataBytes = aesCipher.doFinal(jsonData.getBytes(StandardCharsets.UTF_8));
            String encryptedData = Base64.getEncoder().encodeToString(encryptedDataBytes);

            // Generate HMAC using JSON data and SEK
            String hmac = encryptionUtil.generateHMAC(jsonData, decryptedSek);

            // Create PostBody object and convert to JSON string
            PostBody postBody = new PostBody(hmac, encryptedData);
            return objectMapper.writeValueAsString(postBody);
        } catch (Exception e) {
            log.error("Error during post body generation: ", e);
            throw new CustomException("POST_BODY_GENERATION_ERROR", "Error occurred generating post body");
        }
    }

}
