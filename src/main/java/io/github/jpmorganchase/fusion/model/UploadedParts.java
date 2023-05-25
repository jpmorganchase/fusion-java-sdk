package io.github.jpmorganchase.fusion.model;

import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode
@ToString
@Builder
public class UploadedParts {

    List<UploadedPart> parts;
}
