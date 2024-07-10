package org.egov.eTreasury.repository;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.egov.eTreasury.model.AuthSek;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

@Component
public class AuthSekRowMapper implements RowMapper<AuthSek> {

    @Override
    public AuthSek mapRow(ResultSet rs, int rowNum) throws SQLException {
        AuthSek authSek = new AuthSek();
        authSek.setAuthToken(rs.getString("auth_token"));
        authSek.setTenantId(rs.getString("tenant_id"));
        authSek.setDecryptedSek(rs.getString("decrypted_sek"));
        authSek.setBillId(rs.getString("bill_id"));
        authSek.setTaskNumber(rs.getString("task_number"));
        authSek.setTotalDue(rs.getDouble("total_due"));
        authSek.setMobileNumber(rs.getString("mobile_number"));
        authSek.setPaidBy(rs.getString("paid_by"));
        authSek.setSessionTime(rs.getLong("session_time"));
        return authSek;
    }
}
