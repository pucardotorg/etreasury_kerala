package org.egov.sbi.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.models.AuditDetails;
import org.egov.sbi.model.TransactionDetails;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class TransactionDetailsRowMapper implements RowMapper<TransactionDetails> {

    @Override
    public TransactionDetails mapRow(ResultSet rs, int rowNum) throws SQLException {

        AuditDetails auditdetails = AuditDetails.builder()
                .createdBy(rs.getString("createdby"))
                .createdTime(rs.getLong("createdtime"))
                .lastModifiedBy(rs.getString("lastmodifiedby"))
                .lastModifiedTime(rs.getLong("lastModifiedTime"))
                .build();

        return TransactionDetails.builder()
                .merchantId(rs.getString("merchant_id"))
                .operatingMode(rs.getString("operating_mode"))
                .merchantCountry(rs.getString("merchant_country"))
                .merchantCurrency(rs.getString("merchant_currency"))
                .postingAmount(rs.getDouble("posting_amount"))
                .otherDetails(rs.getString("other_details"))
                .successUrl(rs.getString("success_url"))
                .failUrl(rs.getString("fail_url"))
                .aggregatorId(rs.getString("aggregator_id"))
                .merchantOrderNumber(rs.getString("merchant_order_number"))
                .merchantCustomerId(rs.getString("merchant_customer_id"))
                .payMode(rs.getString("pay_mode"))
                .accessMedium(rs.getString("access_medium"))
                .transactionSource(rs.getString("transaction_source"))
                .auditDetails(auditdetails)
                .sbiEpayRefId(rs.getString("sbi_epay_ref_id"))
                .transactionStatus(rs.getString("transaction_status"))
                .reason(rs.getString("reason"))
                .bankCode(rs.getString("bank_code"))
                .bankReferenceNumber(rs.getString("bank_reference_number"))
                .transactionDate(rs.getString("transaction_date"))
                .cin(rs.getString("cin"))
                .totalFeeGst(rs.getDouble("total_fee_gst"))
                .rowVersion(rs.getInt("row_version"))
                .ref1(rs.getString("ref1"))
                .ref2(rs.getString("ref2"))
                .ref3(rs.getString("ref3"))
                .ref4(rs.getString("ref4"))
                .ref5(rs.getString("ref5"))
                .ref6(rs.getString("ref6"))
                .ref7(rs.getString("ref7"))
                .ref8(rs.getString("ref8"))
                .ref9(rs.getString("ref9"))
                .build();
    }
}
