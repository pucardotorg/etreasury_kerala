package org.egov.sbi.enrichemnt;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.sbi.config.PaymentConfiguration;
import org.egov.sbi.model.TransactionRequest;
import org.egov.sbi.util.IdgenUtil;
import org.egov.tracer.model.CustomException;
import org.springframework.stereotype.Component;

import static org.egov.sbi.config.ServiceConstants.ENRICHMENT_EXCEPTION;

@Component
@Slf4j
public class PaymentEnrichment {

    private final IdgenUtil idgenUtil;

    private final PaymentConfiguration config;

    public PaymentEnrichment(IdgenUtil idgenUtil, PaymentConfiguration config) {
        this.idgenUtil = idgenUtil;
        this.config = config;
    }

    public void enrichTransaction(TransactionRequest request) {
        try {
            String merchantOrderNumber = idgenUtil.getIdList(request.getRequestInfo(), config.getEgovStateTenantId(), config.getIdName(), null, 1).get(0);
            request.getTransactionDetails().setMerchantOrderNumber(merchantOrderNumber);
            AuditDetails auditDetails = AuditDetails.builder().createdBy(request.getRequestInfo().getUserInfo().getUuid()).createdTime(System.currentTimeMillis()).lastModifiedBy(request.getRequestInfo().getUserInfo().getUuid()).lastModifiedTime(System.currentTimeMillis()).build();
            request.getTransactionDetails().setAuditDetails(auditDetails);
            request.getTransactionDetails().setSuccessUrl(config.getSbiTransactionSuccessUrl());
            request.getTransactionDetails().setFailUrl(config.getSbiTransactionFailUrl());
            request.getTransactionDetails().setMerchantId(config.getSbiMerchantId());
            request.getTransactionDetails().setAggregatorId("SBIEPAY");
            request.getTransactionDetails().setMerchantCustomerId("NA");
            request.getTransactionDetails().setAccessMedium("ONLINE");
            request.getTransactionDetails().setTransactionSource("ONLINE");
        } catch (Exception e) {
            log.error("Error enriching transaction request :: {}", e.toString());
            throw new CustomException(ENRICHMENT_EXCEPTION, e.getMessage());
        }
    }
}
