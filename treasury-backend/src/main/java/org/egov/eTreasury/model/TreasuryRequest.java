package org.egov.eTreasury.model;

import org.egov.common.contract.request.RequestInfo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TreasuryRequest {

    
    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("TreasuryParams")
    private TreasuryParams treasuryParams;
}
