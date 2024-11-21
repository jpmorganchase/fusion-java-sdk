package io.github.jpmorganchase.fusion.serializing.mutation;

import io.github.jpmorganchase.fusion.api.context.APIContext;
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

    APIContext apiContext;
    Map<String, Object> varArgs;
}
