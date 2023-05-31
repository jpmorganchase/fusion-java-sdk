package io.github.jpmorganchase.fusion.api.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class ContentRange {

    Long start;
    Long end;
    Long total;
}
