package io.github.jpmorganchase.fusion.serializing.mutation;

import io.github.jpmorganchase.fusion.model.CatalogResource;

@FunctionalInterface
public interface ResourceMutationFactory<T extends CatalogResource> {
    T mutate(T resource, MutationContext mc);
}
