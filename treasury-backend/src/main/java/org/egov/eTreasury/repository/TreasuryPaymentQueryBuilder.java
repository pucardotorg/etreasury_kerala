package org.egov.eTreasury.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@Slf4j
public class TreasuryPaymentQueryBuilder {

    private final String BASE_QUERY = "SELECT file_store_id ";

    private static final String FROM_TABLES = " FROM treasury_payment_data ";

    private static final String DEPARTMENT_ID = " SELECT department_id FROM auth_sek_session_data WHERE ";

    public String getAuthSekQuery(String billId, List<String> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_QUERY);
        query.append(FROM_TABLES);

        if (StringUtils.hasText(billId)) {
            addClauseIfRequired(query, preparedStmtList);
            query.append(" department_id in (");
            query.append(DEPARTMENT_ID).append(" bill_id = ? )");
            preparedStmtList.add(billId);
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
