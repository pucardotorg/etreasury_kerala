package org.egov.eTreasury.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.eTreasury.model.TreasuryPaymentData;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
@Slf4j
public class TreasuryPaymentRowMapper implements RowMapper<TreasuryPaymentData> {
    @Override
    public TreasuryPaymentData mapRow(ResultSet rs, int rowNum) throws SQLException {
        TreasuryPaymentData treasuryPaymentData = TreasuryPaymentData.builder()
                .fileStoreId(rs.getString("file_store_id")).build();
        return treasuryPaymentData;
    }
}
