package io.github.jpmorganchase.fusion.api.request;

import io.github.jpmorganchase.fusion.api.response.Head;
import java.util.Objects;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Used by the {@link PartFetcher} to request a part. The partNo is mandatory.
 * The Head object is optional, omitting indicates the {@link Head} should be
 * returned from the part response.
 */
@Builder
@Getter
@ToString
@EqualsAndHashCode
public class PartRequest {

    int partNo;
    DownloadRequest downloadRequest;
    Head head;

    public boolean isHeadRequired() {
        return Objects.isNull(head);
    }

    public boolean isSinglePartDownloadRequest() {
        return Objects.nonNull(head);
    }

    public boolean isHeadRequest() {
        return 0 == partNo;
    }
}
