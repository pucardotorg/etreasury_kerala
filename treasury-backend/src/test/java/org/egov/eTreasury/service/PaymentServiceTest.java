package org.egov.eTreasury.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.contract.models.Document;
import org.egov.common.contract.request.RequestInfo;
import org.egov.eTreasury.config.PaymentConfiguration;
import org.egov.eTreasury.kafka.Producer;
import org.egov.eTreasury.model.*;
import org.egov.eTreasury.repository.AuthSekRepository;
import org.egov.eTreasury.repository.TreasuryPaymentRepository;
import org.egov.eTreasury.util.*;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private PaymentConfiguration config;

    @Mock
    private ETreasuryUtil treasuryUtil;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Producer producer;

    @Mock
    private AuthSekRepository repository;

    @Mock
    private CollectionsUtil collectionsUtil;

    @Mock
    private FileStorageUtil fileStorageUtil;

    @Mock
    private IdgenUtil idgenUtil;

    @Mock
    private TreasuryPaymentRepository treasuryPaymentRepository;

    @Mock
    private EncryptionUtil encryptionUtil;

    @Mock
    private ConnectionStatus expectedStatus;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ResponseEntity responseEntity;

    @Test
    public void testVerifyConnection_Success() throws Exception {
        // Setup mock objects and responses
        String responseBody = "{\"status\":\"success\"}";
        ConnectionStatus expectedStatus = new ConnectionStatus(); // Initialize as needed
        expectedStatus.setStatus("success"); // or any other method to set the expected value

        // Mock the responses
        when(config.getServerStatusUrl()).thenReturn("http://test.url");
        ResponseEntity<String> expectedResponse = new ResponseEntity<>(responseBody, HttpStatus.OK);
        when(treasuryUtil.callConnectionService(eq("http://test.url"), eq(String.class)))
                .thenReturn(expectedResponse);

        // Mock the ObjectMapper.readValue call
        when(objectMapper.readValue(eq(responseBody), eq(ConnectionStatus.class)))
                .thenReturn(expectedStatus);

        // Call the method under test
        ConnectionStatus actualStatus = paymentService.verifyConnection();

        // Verify results
        assertEquals(expectedStatus, actualStatus);
    }




    @Test
    public void testVerifyConnection_Exception() {
        // Setup mock objects and responses
        when(config.getServerStatusUrl()).thenReturn("http://test.url");
        when(treasuryUtil.callConnectionService(eq("http://test.url"), eq(String.class)))
                .thenThrow(new RuntimeException("Connection error"));

        // Call the method under test and expect an exception
        CustomException thrownException = assertThrows(CustomException.class, () -> {
            paymentService.verifyConnection();
        });

        // Verify exception details
        assertEquals("ETREASURY_CONNECTION_ERROR", thrownException.getCode());
    }

    @Test
    public void testPrintPayInSlip_Success() throws Exception {
        // Setup mock objects and responses
        Map<String, String> secretMap = new HashMap<>();
        secretMap.put("sek", "encryptedSek");
        secretMap.put("appKey", "appKeyValue");
        secretMap.put("authToken", "authTokenValue");

        String decryptedSek = "decryptedSek";
        String postBody = "postBody";
        String headersData = "headersData";
        byte[] responseBody = "responseBody".getBytes(); // Mock response body

        PrintDetails printDetails = new PrintDetails();
        RequestInfo requestInfo = new RequestInfo();

        Document expectedDocument = new Document(); // Assuming Document is the class returned by saveDocumentToFileStore

        // Mocking behavior
//        when(paymentService.authenticate()).thenReturn(secretMap); // Mock private method for test purposes
        when(encryptionUtil.decryptAES(anyString(), anyString())).thenReturn(decryptedSek);
        when(objectMapper.writeValueAsString(any())).thenReturn(postBody, headersData);
        when(config.getPrintSlipUrl()).thenReturn("http://test.url");
        when(treasuryUtil.callService(eq(headersData), eq(postBody), config.getPrintSlipUrl(), any(), eq(MediaType.MULTIPART_FORM_DATA)))
                .thenReturn(new ResponseEntity<>(responseBody, HttpStatus.OK));
        when(fileStorageUtil.saveDocumentToFileStore(any(byte[].class))).thenReturn(expectedDocument);

        // Call the method under test
        Document actualDocument = paymentService.printPayInSlip(printDetails, requestInfo);

        // Verify results
        assertEquals(expectedDocument, actualDocument);

        // Verify interactions with mocks
//        verify(paymentService).authenticate();
        verify(encryptionUtil).decryptAES(anyString(), anyString());
        verify(objectMapper, times(2)).writeValueAsString(any());
        verify(treasuryUtil).callService(eq(headersData), eq(postBody), eq(config.getPrintSlipUrl()), eq(byte[].class), eq(MediaType.MULTIPART_FORM_DATA));
        verify(fileStorageUtil).saveDocumentToFileStore(any(byte[].class));
    }

    @Test
    public void testPrintPayInSlip_Failure() throws Exception {
        // Setup mock objects and responses
        PrintDetails printDetails = new PrintDetails();
        RequestInfo requestInfo = new RequestInfo();

        // Mocking behavior to throw an exception
//        when(paymentService.authenticate()).thenThrow(new RuntimeException("Authentication failed"));

        // Call the method under test and expect exception
        CustomException thrownException = null;
        try {
            paymentService.printPayInSlip(printDetails, requestInfo);
        } catch (CustomException e) {
            thrownException = e;
        }

        // Verify exception
        assertNotNull(thrownException);
//        assertEquals("PRINT_SLIP_ERROR", thrownException.getErrorCode());

        // Verify interactions with mocks
//        verify(paymentService).authenticate();
    }

}
