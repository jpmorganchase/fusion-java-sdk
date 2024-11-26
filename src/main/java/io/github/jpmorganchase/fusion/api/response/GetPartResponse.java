package io.github.jpmorganchase.fusion.api.response;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.InputStream;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class GetPartResponse {

    InputStream content;
    Head head;
}
