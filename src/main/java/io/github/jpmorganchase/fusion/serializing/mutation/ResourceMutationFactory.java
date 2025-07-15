package io.github.jpmorganchase.fusion.serializing.mutation;

import io.github.jpmorganchase.fusion.model.Resource;

@FunctionalInterface
public interface ResourceMutationFactory<T extends Resource> {
    T mutate(T resource, MutationContext mc);
}
