package org.egov.eTreasury.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ChallanData {

    @JsonProperty("ChallanDetails")
    private ChallanDetails challanDetails;

    @JsonProperty("billId")
    private String billId;

    @JsonProperty("taskNumber")
    private String taskNumber;

    @JsonProperty("totalDue")
    private double totalDue;

    @JsonProperty("mobileNumber")
    private String mobileNumber;

    @JsonProperty("paidBy")
    private String paidBy;
}