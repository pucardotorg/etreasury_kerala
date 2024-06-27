package org.egov.eTreasury.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReturnData {


    private boolean status;

    private String rek;

    private String data;

    private String hmac;
}
