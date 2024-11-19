package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.model.Dataset;

public interface Builders {

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.Dataset} object.
     * The builder can be used to set properties and then create / update an instance of Dataset.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.Dataset.DatasetBuilder} the dataset builder
     */
    Dataset.DatasetBuilder dataset();
}
