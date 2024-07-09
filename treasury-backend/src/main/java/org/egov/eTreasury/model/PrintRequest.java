package org.egov.eTreasury.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.egov.common.contract.request.RequestInfo;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PrintRequest {

    @JsonProperty("RequestInfo")
    private RequestInfo requestInfo;

    @JsonProperty("PrintDetails")
    private PrintDetails printDetails;
}
