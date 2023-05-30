package io.github.jpmorganchase.fusion.api.request;

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
}
