package io.github.jpmorganchase.fusion.api.request;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class DownloadRequest {

    private static final String SEPARATOR = "/";

    private String apiPath;
    private String catalog;
    private String dataset;
    private String filePath;
    private boolean isDownloadToStream;
    private Map<String, String> headers;
}
