package org.egov.sbi.service;

import lombok.extern.slf4j.Slf4j;
import org.egov.sbi.config.PaymentConfiguration;
import org.egov.sbi.enrichemnt.PaymentEnrichment;
import org.egov.sbi.kafka.Producer;
import org.egov.sbi.model.BrowserDetails;
import org.egov.sbi.model.BrowserRequest;
import org.egov.sbi.model.TransactionDetails;
import org.egov.sbi.model.TransactionRequest;
import org.egov.sbi.repository.TransactionDetailsRepository;
import org.egov.sbi.util.AES256Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.spec.SecretKeySpec;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class PaymentService {

    private final PaymentEnrichment paymentEnrichment;

    private final Producer producer;

    private final AES256Util aes256Util;

    private final PaymentConfiguration config;

    private final TransactionDetailsRepository repository;

    @Autowired
    public PaymentService(PaymentEnrichment paymentEnrichment, Producer producer, AES256Util aes256Util, PaymentConfiguration config, TransactionDetailsRepository repository) {
        this.paymentEnrichment = paymentEnrichment;
        this.producer = producer;
        this.aes256Util = aes256Util;
        this.config = config;
        this.repository = repository;
    }


    public Map<String, String> processTransaction(TransactionRequest request) {
        paymentEnrichment.enrichTransaction(request);
        String transactionString = request.getTransactionDetails().toString();
        SecretKeySpec secretKeySpec = aes256Util.readKeyBytes(config.getSbiSecretKey());
        String encryptedString = aes256Util.encrypt(transactionString, secretKeySpec);
        String transactionUrl = config.getSbiTransactionUrl();

        Map<String, String> transactionMap = new HashMap<>();
        transactionMap.put("encryptedString", encryptedString);
        transactionMap.put("transactionUrl", transactionUrl);

        producer.push("save-transaction-details", request);
        return transactionMap;
    }

    public TransactionDetails decryptBrowserPayload(BrowserRequest request) {
        String encryptedPayload = request.getEncryptedPayload();
        SecretKeySpec secretKeySpec = aes256Util.readKeyBytes(config.getSbiSecretKey());
        String decryptedPayloadString = aes256Util.decrypt(encryptedPayload, secretKeySpec);
        BrowserDetails browserDetails = BrowserDetails.fromString(decryptedPayloadString);

        TransactionDetails transactionDetails = repository.getTransactionDetails(browserDetails.getMerchantOrderNumber())
                .stream().findFirst().get();

        paymentEnrichment.enrichTransactionResponse(transactionDetails, browserDetails);

        TransactionRequest transactionRequest =  TransactionRequest.builder()
                .requestInfo(request.getRequestInfo()).transactionDetails(transactionDetails).build();
        producer.push("update-transaction-details", transactionRequest);

        return transactionDetails;
    }
}
