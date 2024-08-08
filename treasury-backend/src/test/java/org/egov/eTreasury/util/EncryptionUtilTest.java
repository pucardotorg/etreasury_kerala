package org.egov.eTreasury.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class EncryptionUtilTest {

    @InjectMocks
    private EncryptionUtil encryptionUtil;

    @Test
    void testGetClientSecretAndAppKey() throws Exception {
        // Arrange
        String clientSecret = "your_client_secret";
        String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq2sGa9hc9BGBV5OLkjMjxUU+1ZsNI1MXgHtbVF3q7/SbRYXxvXUTX5bFZDvHo4tVTl6xoE2sIjoxnHVqq2JGXPVD"; // Example public key

        // Act
        Map<String, String> secretMap = encryptionUtil.getClientSecretAndAppKey(clientSecret, publicKeyString);

        // Assert
        Assertions.assertNotNull(secretMap);
        Assertions.assertNotNull(secretMap.get("appKey"));
        Assertions.assertNotNull(secretMap.get("encryptedClientSecret"));
        Assertions.assertNotNull(secretMap.get("encodedAppKey"));
    }

    @Test
    void testDecryptAES() throws Exception {
        // Arrange
        String encryptedData = "encrypted_data";
        String key = "your_base64_encoded_key";

        // Act
        String decryptedData = encryptionUtil.decryptAES(encryptedData, key);

        // Assert
        Assertions.assertNotNull(decryptedData);
    }

    @Test
    void testGenerateHMAC() throws Exception {
        // Arrange
        String data = "data_to_sign";
        String key = "your_hmac_key";

        // Act
        String hmac = encryptionUtil.generateHMAC(data, key);

        // Assert
        Assertions.assertNotNull(hmac);
        Assertions.assertTrue(hmac.length() > 0);
    }

    @Test
    void testDecryptResponse() throws Exception {
        // Arrange
        String encryptedData = "encrypted_response_data";
        String key = "your_aes_key";

        // Act
        String decryptedData = encryptionUtil.decryptResponse(encryptedData, key);

        // Assert
        Assertions.assertNotNull(decryptedData);
    }

//    @Test
//    void testGetPublicKeyFromString() throws NoSuchAlgorithmException, InvalidKeySpecException {
//        // Arrange
//        String publicKeyString = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAq2sGa9hc9BGBV5OLkjMjxUU+1ZsNI1MXgHtbVF3q7/SbRYXxvXUTX5bFZDvHo4tVTl6xoE2sIjoxnHVqq2JGXPVD";
//
//        // Act
//        PublicKey publicKey = encryptionUtil.getPublicKeyFromString(publicKeyString);
//
//        // Assert
//        Assertions.assertNotNull(publicKey);
//        Assertions.assertTrue(publicKey instanceof PublicKey);
//    }
//
//    @Test
//    void testGetPublicKeyFromString_ThrowsException() {
//        // Arrange
//        String invalidPublicKeyString = "invalid_public_key";
//
//        // Act and Assert
//        Assertions.assertThrows(InvalidKeySpecException.class, () -> encryptionUtil.getPublicKeyFromString(invalidPublicKeyString));
//    }

//    @Test
//    void testBytesToEncodedString() {
//        // Arrange
//        byte[] bytes = {0x01, 0x02, 0x03};
//
//        // Act
//        String encodedString = encryptionUtil.bytesToEncodedString(bytes);
//
//        // Assert
//        Assertions.assertNotNull(encodedString);
//        Assertions.assertTrue(encodedString.length() > 0);
//    }
}