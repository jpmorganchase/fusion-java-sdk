package io.github.jpmorganchase.fusion.api.context;

import io.github.jpmorganchase.fusion.api.APIManager;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode
@ToString
public class APIContext {

    APIManager apiManager;
    String rootUrl;
    String defaultCatalog;
}
