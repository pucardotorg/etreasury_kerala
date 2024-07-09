package org.egov.eTreasury.repository;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AuthSekQueryBuilder {

    private final String BASE_QUERY = "SELECT auth_token, tenant_id, decrypted_sek, bill_id, task_number, total_due, mobile_number, paid_by, session_time ";

    private static final String FROM_TABLES = " FROM auth_sek_session_data ";

    public String getAuthSekQuery(String authToken, List<String> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        query.append(FROM_TABLES);

        if (!StringUtils.hasText(authToken)) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" auth_token = ? ");
            preparedStmtList.add(authToken);
        }

        return query.toString();
    }

    private void addClauseIfRequired(StringBuilder query, List<String> preparedStmtList) {
        if (preparedStmtList.isEmpty()) {
            query.append(" WHERE ");
        } else {
            query.append(" AND ");
        }
    }
}
