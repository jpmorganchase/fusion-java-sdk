package io.github.jpmorganchase.fusion.serializing.mutation;

import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@Builder
@EqualsAndHashCode
@ToString
public class MutationContext {

    Map<String, Object> varArgs;
}
