package io.github.jpmorganchase.fusion.api.response;

import java.io.InputStream;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder
@ToString
@EqualsAndHashCode
public class GetPartResponse {

    InputStream content;
    Head head;
}
