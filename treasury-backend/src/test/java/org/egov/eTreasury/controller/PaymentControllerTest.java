package org.egov.eTreasury.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

import org.egov.common.contract.models.Document;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.egov.eTreasury.model.*;
import org.egov.eTreasury.service.PaymentService;
import org.egov.eTreasury.util.ResponseInfoFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(MockitoExtension.class)
public class PaymentControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PaymentService paymentService;

    @Mock
    private ResponseInfoFactory responseInfoFactory;

    @InjectMocks
    private PaymentController paymentController;

    @BeforeEach
    public void setup() {

    }

    @Test
    public void testVerifyServerConnection(){
        // Arrange
        ConnectionStatus connectionStatus = ConnectionStatus.builder().status("success").build();
        ResponseInfo responseInfo = new ResponseInfo();
        ConnectionResponse expectedResponse = ConnectionResponse.builder()
                .responseInfo(responseInfo)
                .connectionStatus(connectionStatus)
                .build();

        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), anyBoolean()))
                .thenReturn(responseInfo);
        when(paymentService.verifyConnection()).thenReturn(connectionStatus);

        // Act
        ConnectionResponse responseEntity = paymentController.verifyServerConnection(
                "default",
                new RequestInfo()
        );

        // Assert
        assertEquals(connectionStatus.getStatus(), responseEntity.getConnectionStatus().getStatus());
    }

    @Test
    public void testProcessPayment(){
        // Arrange
        Payload payload = new Payload();
        ResponseInfo responseInfo = new ResponseInfo();
        HtmlResponse expectedResponse = HtmlResponse.builder()
                .payload(payload)
                .responseInfo(responseInfo)
                .build();

//        when(responseInfoFactory.createResponseInfoFromRequestInfo(any(RequestInfo.class), eq(false)))
//                .thenReturn(responseInfo);
        when(paymentService.processPayment(any(), any())).thenReturn(payload);

        // Act
        HtmlResponse responseEntity = paymentController.processPayment(new ChallanRequest());


    }

    @Test
    public void testPrintPayInSlip() {
        // Arrange
        Document document = new Document();
        ResponseInfo responseInfo = new ResponseInfo();
        PrintResponse expectedResponse = PrintResponse.builder()
                .responseInfo(responseInfo)
                .document(document)
                .build();



        when(paymentService.printPayInSlip(any(), any())).thenReturn(document);

        // Act
        PrintResponse responseEntity = paymentController.printPayInSlip(new PrintRequest());


    }

    @Test
    public void testDecryptTreasuryResponse() {
        // Arrange
        ResponseInfo responseInfo = new ResponseInfo();

        // Act
        ResponseEntity<ResponseInfo> responseEntity = paymentController.decryptTreasuryResponse(new TreasuryRequest());

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
    }

    @Test
    public void testGetTreasuryPaymentReceipt() throws Exception {
        // Arrange
        Document document = new Document();
        ResponseInfo responseInfo = new ResponseInfo();
        PrintResponse expectedResponse = PrintResponse.builder()
                .responseInfo(responseInfo)
                .document(document)
                .build();


        when(paymentService.getTreasuryPaymentData(anyString())).thenReturn(document);

        // Act
        PrintResponse responseEntity = paymentController.getTreasuryPaymentReceipt(
                "bill123",
                new RequestInfo()
        );

        // Assert
//        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
//        assertEquals(expectedResponse, responseEntity.getBody());
    }
}
