package org.egov.eTreasury.config;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class PaymentConfiguration {

    //Tenant Id
    @Value("${egov-state-level-tenant-id}")
    private String egovStateTenantId;

    //ETreasury
    @Value("${treasury-public-key}")
    private String publicKey;

    @Value("${treasury-client-secret}")
    private String clientSecret;

    @Value("${treasury-client-id}")
    private String clientId;

    @Value("${treasury-auth-url}")
    private String authUrl;

    @Value("${treasury-challan-generate-url}")
    private String challanGenerateUrl;

    @Value("${treasury-double-verification-url}")
    private String doubleVerificationUrl;

    @Value("${treasury-print-slip-url}")
    private String printSlipUrl;

    @Value("${treasury-transaction-details-url")
    private String transactionDetailsUrl;
}
