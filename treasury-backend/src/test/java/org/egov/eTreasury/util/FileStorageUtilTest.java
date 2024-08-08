package org.egov.eTreasury.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.egov.common.contract.models.Document;
import org.egov.eTreasury.config.PaymentConfiguration;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FileStorageUtilTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private PaymentConfiguration paymentConfiguration;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private FileStorageUtil fileStorageUtil;

    @Mock
    private FileStorageUtil fileStorageUtil1;



    @Test
    void saveDocumentToFileStore_ShouldReturnDocument() throws JsonProcessingException {
        // Arrange
        byte[] payInSlipBytes = "test content".getBytes();
        String fileStoreId = "testFileStoreId";
        String responseBody = "{\"files\":[{\"fileStoreId\":\"" + fileStoreId + "\"}]}";
        ResponseEntity<Object> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        // Mock PaymentConfiguration
        when(paymentConfiguration.getFileStoreHost()).thenReturn("http://localhost");
        when(paymentConfiguration.getFileStoreEndPoint()).thenReturn("/api/files");
        when(paymentConfiguration.getEgovStateTenantId()).thenReturn("tenantId");
        when(paymentConfiguration.getTreasuryFileStoreModule()).thenReturn("treasury");

        // Mock RestTemplate
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Object.class)))
                .thenReturn(responseEntity);

        // Mock ObjectMapper
        ObjectNode rootNode = objectMapper.createObjectNode();
        ArrayNode filesArray = objectMapper.createArrayNode();
        ObjectNode fileNode = objectMapper.createObjectNode();
        fileNode.put("fileStoreId", "testFileStoreId");
        filesArray.add(fileNode);
        rootNode.set("files", filesArray);

        // Cast ObjectNode to JsonNode
        JsonNode jsonNode = rootNode;
        // Mock ObjectMapper
//        ObjectMapper objectMapper = new ObjectMapper();
        when(objectMapper.convertValue(responseEntity.getBody(), JsonNode.class)).thenReturn(jsonNode);

        Document document = fileStorageUtil.saveDocumentToFileStore(payInSlipBytes);

        // Assert
        assertEquals(fileStoreId, document.getFileStore());
        assertEquals("application/pdf", document.getDocumentType());
    }


    @Test
    void saveDocumentToFileStore_ShouldThrowExceptionOnFailure() {
        // Arrange
        byte[] payInSlipBytes = "test content".getBytes();
        when(paymentConfiguration.getFileStoreHost()).thenReturn("http://localhost");
        when(paymentConfiguration.getFileStoreEndPoint()).thenReturn("/api/files");
        when(paymentConfiguration.getEgovStateTenantId()).thenReturn("tenantId");
        when(paymentConfiguration.getTreasuryFileStoreModule()).thenReturn("treasury");
        when(restTemplate.postForEntity(any(String.class), any(HttpEntity.class), eq(Object.class)))
                .thenThrow(new RuntimeException("Test Exception"));

        // Act & Assert
        CustomException thrown = assertThrows(CustomException.class, () ->
                fileStorageUtil.saveDocumentToFileStore(payInSlipBytes));
        assertEquals("TREASURY_FILE_STORE_ERROR", thrown.getCode());
        assertEquals("Error occurred when getting saving document in File Store", thrown.getMessage());
    }
}
