package org.egov.sbi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.egov.common.contract.models.AuditDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionDetails {

    @JsonProperty("MerchantId")
    private String merchantId;

    @JsonProperty("OperatingMode")
    private String operatingMode;

    @JsonProperty("MerchantCountry")
    private String merchantCountry;

    @JsonProperty("MerchantCurrency")
    private String merchantCurrency;

    @JsonProperty("PostingAmount")
    private double postingAmount;

    @JsonProperty("OtherDetails")
    private String otherDetails;

    @JsonProperty("SuccessURL")
    private String successUrl;

    @JsonProperty("FailURL")
    private String failUrl;

    @JsonProperty("AggregatorId")
    private String aggregatorId;

    @JsonProperty("MerchantOrderNumber")
    private String merchantOrderNumber;

    @JsonProperty("MerchantCustomerId")
    private String merchantCustomerId;

    @JsonProperty("PayMode")
    private String payMode;

    @JsonProperty("AccessMedium")
    private String accessMedium;

    @JsonProperty("TransactionSource")
    private String transactionSource;

    @JsonProperty("AuditDetails")
    private AuditDetails auditDetails;

    @Override
    public String toString() {
        return merchantId + "|" +
                operatingMode + "|" +
                merchantCountry + "|" +
                merchantCurrency + "|" +
                postingAmount + "|" +
                otherDetails + "|" +
                successUrl + "|" +
                failUrl + "|" +
                aggregatorId + "|" +
                merchantOrderNumber + "|" +
                merchantCustomerId + "|" +
                payMode + "|" +
                accessMedium + "|" +
                transactionSource;
    }
}
