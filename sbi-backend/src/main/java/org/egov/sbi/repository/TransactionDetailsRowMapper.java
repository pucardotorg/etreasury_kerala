package org.egov.sbi.repository;

import lombok.extern.slf4j.Slf4j;
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
        return null;
    }
}
