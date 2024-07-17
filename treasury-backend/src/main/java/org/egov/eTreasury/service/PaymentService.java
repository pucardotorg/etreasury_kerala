package org.egov.eTreasury.service;

import org.egov.common.contract.models.Document;
import org.egov.common.contract.request.RequestInfo;
import org.egov.eTreasury.config.PaymentConfiguration;
import org.egov.eTreasury.kafka.Producer;
import org.egov.eTreasury.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import digit.models.coremodels.PaymentDetail;
import lombok.extern.slf4j.Slf4j;
import org.egov.eTreasury.model.*;
import org.egov.eTreasury.repository.AuthSekRepository;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class PaymentService {

    private final PaymentConfiguration config;

    private final ETreasuryUtil treasuryUtil;

    private final ObjectMapper objectMapper;

    private final EncryptionUtil encryptionUtil;

    private final Producer producer;

    private final AuthSekRepository repository;

    private final CollectionsUtil collectionsUtil;

    private final FileStorageUtil fileStorageUtil;

    @Autowired
    public PaymentService(PaymentConfiguration config, ETreasuryUtil treasuryUtil,
                          ObjectMapper objectMapper, EncryptionUtil encryptionUtil,
                          Producer producer, AuthSekRepository repository, CollectionsUtil collectionsUtil, FileStorageUtil fileStorageUtil) {
        this.config = config;
        this.treasuryUtil = treasuryUtil;
        this.objectMapper = objectMapper;
        this.encryptionUtil = encryptionUtil;
        this.producer = producer;
        this.repository = repository;
        this.collectionsUtil = collectionsUtil;
        this.fileStorageUtil = fileStorageUtil;
    }

    public ConnectionStatus verifyConnection() {
        try {
            ResponseEntity<ConnectionStatus> responseEntity = treasuryUtil.callConnectionService(config.getServerStatusUrl(), ConnectionStatus.class);
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                return responseEntity.getBody();
            } else {
                throw new CustomException("AUTHENTICATION_FAILED", "Authentication request failed with status: " + responseEntity.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Establishing a connection with ETreasury server failed: ", e);
            throw new CustomException("ETREASURY_CONNECTION_ERROR", "Error occurred when establishing connection with ETreasury server");
        }
    }

    private Map<String, String> authenticate() {
        Map<String, String> secretMap;
        try {
            // Generate client secret and app key
            secretMap = encryptionUtil.getClientSecretAndAppKey(config.getClientSecret(), config.getPublicKey());
            // Prepare authentication request payload
            AuthRequest authRequest = new AuthRequest(secretMap.get("encodedAppKey"));
            String payload = objectMapper.writeValueAsString(authRequest);
            // Call the authentication service
            ResponseEntity<?> responseEntity = treasuryUtil.callAuthService(config.getClientId(), secretMap.get("encryptedClientSecret"),
            payload, config.getAuthUrl());
            log.info("Status Code: {}", responseEntity.getStatusCode());
            log.info("Response Body: {}", responseEntity.getBody());
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

    public HtmlPage processPayment(ChallanData challanData, RequestInfo requestInfo) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            //TODO get department id and fill it
            AuthSek authSek = AuthSek.builder()
                .authToken(secretMap.get("authToken"))
                .decryptedSek(decryptedSek)
                .billId(challanData.getBillId())
                .businessService(challanData.getBusinessService())
                .serviceNumber(challanData.getServiceNumber())
                .mobileNumber(challanData.getMobileNumber())
                .totalDue(challanData.getTotalDue())
                .paidBy(challanData.getPaidBy())
                .sessionTime(System.currentTimeMillis()).build();
            saveAuthTokenAndSek(requestInfo, authSek);

            // Prepare the request body
            challanData.getChallanDetails().setServiceDeptCode(config.getServiceDeptCode());
            challanData.getChallanDetails().setOfficeCode(config.getOfficeCode());
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(challanData.getChallanDetails()));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<String> responseEntity = callService(headersData, postBody, config.getChallanGenerateUrl(), String.class, MediaType.TEXT_HTML);
            String htmlString = responseEntity.getBody();
            String scriptTag = "\n<script src=\"https://code.jquery.com/jquery-3.6.0.min.js\"></script>";
            htmlString = scriptTag + htmlString;
            return HtmlPage.builder().htmlString(htmlString).build();
        } catch (Exception e) {
            log.error("Payment processing error: ", e);
            throw new CustomException("PAYMENT_PROCESSING_ERROR", "Error occurred during generation oF challan");
        }
    }

    public HtmlPage doubleVerifyPayment(VerificationData verificationData, RequestInfo requestInfo) {
        try {
            VerificationDetails verificationDetails = verificationData.getVerificationDetails();
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));
            AuthSek authSek = AuthSek.builder()
                    .authToken(secretMap.get("authToken"))
                    .decryptedSek(decryptedSek)
                    .billId(verificationData.getBillId())
                    .businessService(verificationData.getBusinessService())
                    .serviceNumber(verificationData.getServiceNumber())
                    .totalDue(verificationData.getTotalDue())
                    .paidBy(verificationData.getPaidBy())
                    .sessionTime(System.currentTimeMillis())
                    .departmentId(verificationDetails.getDepartmentId()).build();
            saveAuthTokenAndSek(requestInfo, authSek);

            // Prepare the request body
            verificationDetails.setOfficeCode(config.getOfficeCode());
            verificationDetails.setServiceDeptCode(config.getServiceDeptCode());
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(verificationDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<String> responseEntity = callService(headersData, postBody, config.getDoubleVerificationUrl(), String.class, MediaType.TEXT_HTML);
            return HtmlPage.builder().htmlString(responseEntity.getBody()).build();
        } catch (Exception e) {
            log.error("Double verification Error: ", e);
            throw new CustomException("DOUBLE_VERIFICATION_ERROR", "Error occurred during double verification");
        }
    }

    public Document printPayInSlip(PrintDetails printDetails, RequestInfo requestInfo) {
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
            ResponseEntity<byte[]> responseEntity = callService(headersData, postBody, config.getPrintSlipUrl(), byte[].class, MediaType.MULTIPART_FORM_DATA);

            // Process the response
            if (responseEntity.getStatusCode().is2xxSuccessful() && responseEntity.getBody() != null) {
                 return fileStorageUtil.saveDocumentToFileStore(responseEntity.getBody());
            } else {
                throw new CustomException("PRINT_SLIP_FAILED", "Pay in slip request failed");
            }
        } catch (Exception e) {
            log.error("Print slip generation Error: ", e);
            throw new CustomException("PRINT_SLIP_ERROR", "Error occurred during pay in slip generation");
        }
    }

    public TransactionDetails fetchTransactionDetails(TransactionDetails transactionDetails, RequestInfo requestInfo) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            transactionDetails.setDepartmentId(config.getDeptReferenceId());
            String postBody = generatePostBody(decryptedSek, objectMapper.writeValueAsString(transactionDetails));

            // Prepare headers
            Headers headers = new Headers();
            headers.setClientId(config.getClientId());
            headers.setAuthToken(secretMap.get("authToken"));
            String headersData = objectMapper.writeValueAsString(headers);

            // Call the service
            ResponseEntity<TransactionDetails> responseEntity = callService(headersData, postBody, config.getTransactionDetailsUrl(), TransactionDetails.class, MediaType.APPLICATION_JSON);
            return objectMapper.convertValue(responseEntity.getBody(), TransactionDetails.class);
        } catch (Exception e) {
            log.error("Transaction details retrieval failed: ", e);
            throw new CustomException("TRANSACTION_DETAILS_ERROR", "Error ccurred during transaction details retrieval");
        }
    }

    public RefundData processRefund(RefundDetails refundDetails, RequestInfo requestInfo) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBodyForRefund(decryptedSek, objectMapper.writeValueAsString(refundDetails));

            // Call the service
            ResponseEntity<TreasuryResponse> responseEntity = treasuryUtil.callRefundService(config.getClientId(), secretMap.get("authToken"), postBody, config.getRefundRequestUrl(), TreasuryResponse.class);
            TreasuryResponse response = responseEntity.getBody();
            String decryptedRek = encryptionUtil.decryptResponse(response.getRek(), decryptedSek);
            String decryptedData = encryptionUtil.decryptResponse(response.getData(), decryptedRek);

            return objectMapper.convertValue(decryptedData, RefundData.class);
        } catch (Exception e) {
            log.error("Refund Request failed: ", e);
            throw new CustomException("REFUND_REQUEST_ERROR", "Error occurred during  refund request");
        }
    }

    public RefundData checkRefundStatus(RefundStatus refundStatus, RequestInfo requestInfo) {
        try {
            // Authenticate and get secret map
            Map<String, String> secretMap = authenticate();

            // Decrypt the SEK using the appKey
            String decryptedSek = encryptionUtil.decryptAES(secretMap.get("sek"), secretMap.get("appKey"));

            // Prepare the request body
            String postBody = generatePostBodyForRefund(decryptedSek, objectMapper.writeValueAsString(refundStatus));

            // Call the service
            ResponseEntity<TreasuryResponse> responseEntity = treasuryUtil.callRefundService(config.getClientId(),
            secretMap.get("authToken"), postBody, config.getRefundStatusUrl(), TreasuryResponse.class);
            TreasuryResponse response = responseEntity.getBody();
            String decryptedRek = encryptionUtil.decryptResponse(response.getRek(), decryptedSek);
            String decryptedData = encryptionUtil.decryptResponse(response.getData(), decryptedRek);

            return objectMapper.convertValue(decryptedData, RefundData.class);
        } catch (Exception e) {
            log.error("Refund Request failed: ", e);
            throw new CustomException("REFUND_REQUEST_ERROR", "Error occurred during  refund request");
        }
    }

    public void decryptAndProcessTreasuryPayload(TreasuryParams treasuryParams, RequestInfo requestInfo) {
        try {
            Optional<AuthSek> optionalAuthSek = repository.getAuthSek(treasuryParams.getAuthToken()).stream().findFirst();
            if (optionalAuthSek.isPresent()) {
                String decryptedSek = optionalAuthSek.get().getDecryptedSek();
                String decryptedRek = encryptionUtil.decryptResponse(treasuryParams.getRek(), decryptedSek);
                String decryptedData = encryptionUtil.decryptResponse(treasuryParams.getData(), decryptedRek);
                TransactionDetails transactionDetails = objectMapper.readValue(decryptedData, TransactionDetails.class);
                // TODO create treasury payment data and from here and store it
                TreasuryPaymentRequest request = TreasuryPaymentRequest.builder()
                        .requestInfo(requestInfo).treasuryPaymentData(null).build();
                producer.push("save-treasury-payment-data", request);
                updatePaymentStatus(optionalAuthSek.get(), transactionDetails, requestInfo);
            }
        } catch (Exception e) {
            log.error("Decrypt Treasury Response failed: ", e);
            throw new CustomException("TREASURY_RESPONSE_ERROR", "Error occurred during decrypting Treasury Response");
        }
    }

    private void saveAuthTokenAndSek(RequestInfo requestInfo, AuthSek authSek) {
        AuthSekRequest request = new AuthSekRequest(requestInfo, authSek);
        producer.push("save-auth-sek", request);
    }

    private <T> ResponseEntity<T> callService(String headersData, String postBody, String url, Class<T> responseType, MediaType mediaType) {
        return treasuryUtil.callService(headersData, postBody, url, responseType, mediaType);
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

    private String generatePostBodyForRefund(String decryptedSek, String jsonData) {
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
            RefundPostBody refundPostBody = new RefundPostBody(hmac, encryptedData);
            return objectMapper.writeValueAsString(refundPostBody);
        } catch (Exception e) {
            log.error("Error during post body generation: ", e);
            throw new CustomException("POST_BODY_GENERATION_ERROR", "Error occurred generating post body");
        }
    }

    private void updatePaymentStatus(AuthSek authSek, TransactionDetails transactionDetails, RequestInfo requestInfo) {
        PrintDetails printDetails = new PrintDetails(transactionDetails.getGrn());
        Document document = printPayInSlip(printDetails, requestInfo);
        PaymentDetail paymentDetail = PaymentDetail.builder()
            .billId(authSek.getBillId())
            .totalDue(BigDecimal.valueOf(authSek.getTotalDue()))
            .totalAmountPaid(new BigDecimal(transactionDetails.getAmount()))
            .businessService(authSek.getBusinessService()).build();
        Payment payment = Payment.builder()
            .tenantId(config.getEgovStateTenantId())
            .paymentDetails(Collections.singletonList(paymentDetail))
            .payerName(transactionDetails.getPartyName())
            .paidBy(authSek.getPaidBy())
            .mobileNumber(authSek.getMobileNumber())
            .transactionNumber(transactionDetails.getGrn())
            .transactionDate(convertTimestampToMillis(transactionDetails.getChallanTimestamp()))
            .instrumentNumber(transactionDetails.getBankRefNo())
            .instrumentDate(convertTimestampToMillis(transactionDetails.getBankTimestamp()))
            .totalAmountPaid(new BigDecimal(transactionDetails.getAmount()))
            .paymentMode("ONLINE")
            .fileStoreId(document.getFileStore())
            .build();
        String paymentStatus = transactionDetails.getStatus();
        if (paymentStatus.equals("Y")) {
            payment.setPaymentStatus("DEPOSITED");
        }
        PaymentRequest paymentRequest = new PaymentRequest(requestInfo, payment);
        collectionsUtil.callService(paymentRequest, config.getCollectionServiceHost(), config.getCollectionsPaymentCreatePath());
    }

    private Long convertTimestampToMillis(String timestampStr) {
        List<DateTimeFormatter> formatters = new ArrayList<>();
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS"));
        formatters.add(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH.mm.ss.SSSSS"));
        LocalDateTime dateTime = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                dateTime = LocalDateTime.parse(timestampStr, formatter);
                break;
            } catch (Exception e) {
                // Try next formatter if parsing fails
            }
        }
        if (dateTime != null) {
            return dateTime.toInstant(ZoneOffset.UTC).toEpochMilli();
        } else {
            return null;
        }
    }
}
