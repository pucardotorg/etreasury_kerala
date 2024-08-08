package org.egov.eTreasury.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(MockitoExtension.class)
public class TreasuryPaymentQueryBuilderTest {

    private TreasuryPaymentQueryBuilder queryBuilder;

    @BeforeEach
    void setUp() {
        queryBuilder = new TreasuryPaymentQueryBuilder();
    }

    @Test
    void testGetTreasuryPaymentQueryWithBillId() {
        // Given
        String billId = "testBillId";
        List<String> preparedStmtList = new ArrayList<>();

        // When
        String query = queryBuilder.getTreasuryPaymentQuery(billId, preparedStmtList);

        // Then
        String expectedQuery = "SELECT department_id, grn, challan_timestamp, bank_ref_no, bank_timestamp, bank_code, status, cin, amount, party_name, remark_status, remarks, file_store_id FROM treasury_payment_data WHERE department_id IN (SELECT department_id FROM auth_sek_session_data WHERE bill_id = ? ORDER BY session_time )";
        assertThat(query).isEqualTo(expectedQuery);
        assertThat(preparedStmtList).containsExactly("testBillId");
    }

    @Test
    void testGetTreasuryPaymentQueryWithoutBillId() {
        // Given
        String billId = null;
        List<String> preparedStmtList = new ArrayList<>();

        // When
        String query = queryBuilder.getTreasuryPaymentQuery(billId, preparedStmtList);

        // Then
        String expectedQuery = "SELECT department_id, grn, challan_timestamp, bank_ref_no, bank_timestamp, bank_code, status, cin, amount, party_name, remark_status, remarks, file_store_id FROM treasury_payment_data ";
        assertThat(query).isEqualTo(expectedQuery);
        assertThat(preparedStmtList).isEmpty();
    }
}
