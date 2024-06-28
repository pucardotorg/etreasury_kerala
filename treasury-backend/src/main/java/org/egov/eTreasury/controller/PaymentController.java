package org.egov.eTreasury.controller;

import org.egov.eTreasury.model.*;
import org.egov.eTreasury.service.PaymentService;
import org.egov.eTreasury.util.ResponseInfoFactory;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    private final ResponseInfoFactory responseInfoFactory;

    public PaymentController(PaymentService paymentService, ResponseInfoFactory responseInfoFactory) {
        this.paymentService = paymentService;
        this.responseInfoFactory = responseInfoFactory;
    }

    @PostMapping("/v1/_processPayment")
    public HtmlResponse processPayment(@RequestBody PaymentRequest request) {
        log.info("Processing payment for request: {}", request);
        HtmlPage paymentPage = paymentService.processPayment(request.getPaymentDetails());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        log.info("Payment processed successfully for request: {}", request);
        return HtmlResponse.builder().htmlPage(paymentPage).responseInfo(responseInfo).build();
    }

    @PostMapping("/v1/_doubleVerification")
    public HtmlResponse verifyDetails(@RequestBody VerificationRequest request) {
        log.info("Performing double verification for request: {}", request);
        HtmlPage verificationPage = paymentService.doubleVerifyPayment(request.getVerificationDetails());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        log.info("Double verification successful for request: {}", request);
        return HtmlResponse.builder().htmlPage(verificationPage).responseInfo(responseInfo).build();
    }

    @PostMapping("/v1/_printPayInSlip")
    public ResponseEntity<?> printPayInSlip(@RequestBody PrintDetails printDetails) {
        log.info("Printing pay-in slip for details: {}", printDetails);
        ByteArrayResource resource = paymentService.printPayInSlip(printDetails);
        log.info("Pay-in slip printed successfully for details: {}", printDetails);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + "Application-" + printDetails.getHcinNo() + ".pdf" + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "application/pdf").body(resource);
    }

    @PostMapping("/v1/_transactionDetails")
    public TransactionResponse processTransaction(@RequestBody TransactionRequest request) {
        log.info("Fetching transaction details for request: {}", request);
        TransactionDetails transactionDetails = paymentService.fetchTransactionDetails(request.getTransactionDetails());
        ResponseInfo responseInfo = responseInfoFactory.createResponseInfoFromRequestInfo(request.getRequestInfo(), true);
        log.info("Transaction details fetched successfully for request: {}", request);
        return TransactionResponse.builder().transactionDetails(transactionDetails).responseInfo(responseInfo).build();
    }
}
