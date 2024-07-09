package org.egov.eTreasury.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TreasuryParams {

    private String authToken;
    private String status;
    private String rek;
    private String hmac;
    private String data;
}
