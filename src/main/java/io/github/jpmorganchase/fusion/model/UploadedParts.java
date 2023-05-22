package io.github.jpmorganchase.fusion.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.List;

@Value
@EqualsAndHashCode
@ToString
@Builder
public class UploadedParts {

    List<UploadedPart> parts;
}
