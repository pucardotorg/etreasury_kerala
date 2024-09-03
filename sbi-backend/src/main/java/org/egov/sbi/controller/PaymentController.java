package org.egov.sbi.controller;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.sbi.model.*;
import org.egov.sbi.service.PaymentService;
import org.egov.sbi.util.ResponseInfoFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    private final ResponseInfoFactory responseInfoFactory;

    @Autowired
    public PaymentController(PaymentService paymentService, ResponseInfoFactory responseInfoFactory) {
        this.paymentService = paymentService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/v1/_processTransaction")
    public TransactionResponse processTransaction(@RequestBody TransactionRequest request) {
        log.info("Processing transaction request: {}", request);
        Map<String, String> transactionMap = paymentService.processTransaction(request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        log.info("Transaction request processed successfully: {}", request);
        return TransactionResponse.builder()
                .encryptedString(transactionMap.get("encryptedString"))
                .transactionUrl(transactionMap.get("transactionUrl"))
                .responseInfo(responseInfo).build();
    }

    @PostMapping("/v1/_decryptBrowserResponse")
    public BrowserResponse decryptTreasuryResponse(@RequestBody BrowserRequest request) {
        log.info("Decrypting Browser Response for request: {}", request);
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        TransactionDetails transactionDetails = paymentService.decryptBrowserPayload(request);
        log.info("Decrypted Browser Response successfully for request: {}", request);
        return BrowserResponse.builder()
                .responseInfo(responseInfo)
                .transactionDetails(transactionDetails)
                .build();
    }
}
