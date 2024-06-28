package org.egov.eTreasury.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HtmlPage {


    @JsonProperty("decryptedSek")
    private String decryptedSek;

    @JsonProperty("htmlString")
    private String htmlString;
}
