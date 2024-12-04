package io.github.jpmorganchase.fusion.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Builder
@Value
@EqualsAndHashCode()
@ToString()
public class Flow {
    String flowDirection;
}
