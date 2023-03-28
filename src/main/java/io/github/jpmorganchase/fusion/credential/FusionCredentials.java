package io.github.jpmorganchase.fusion.credential;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@Builder
public class FusionCredentials implements Credentials {
    private String token;
    private String authServerUrl;
    private String catalog;
    private String dataset;
}
