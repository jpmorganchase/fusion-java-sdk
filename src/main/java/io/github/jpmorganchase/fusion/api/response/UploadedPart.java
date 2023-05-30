package io.github.jpmorganchase.fusion.api.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode
@ToString
@Builder
public class UploadedPart {

    String partNumber;
    String partIdentifier;
    String partDigest;
}
