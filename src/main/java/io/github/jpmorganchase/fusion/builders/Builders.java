package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.model.Attribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttribute;
import io.github.jpmorganchase.fusion.model.DataDictionaryAttributes;
import io.github.jpmorganchase.fusion.model.Dataset;

public interface Builders {

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.Dataset} object.
     * The builder can be used to set properties and then create / update an instance of Dataset.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.Dataset.DatasetBuilder} the dataset builder
     */
    Dataset.DatasetBuilder dataset();

    /**
     * Returns a builder for creating a {@link DataDictionaryAttribute} object.
     * The builder can be used to set properties and then create / update an instance of Attribute.
     *
     * @return {@link DataDictionaryAttribute.DataDictionaryAttributeBuilder} the attribute builder
     */
    DataDictionaryAttribute.DataDictionaryAttributeBuilder dataDictionaryAttribute();

    /**
     * Returns a builder for creating a {@link DataDictionaryAttributes} object.
     * <p>
     * The builder allows you to set properties and construct an instance of {@link DataDictionaryAttributes}.
     * This is useful for managing collections or groups of related attributes.
     * </p>
     *
     * @return a {@link DataDictionaryAttributes.DataDictionaryAttributesBuilder} instance for building a {@link DataDictionaryAttributes}.
     */
    DataDictionaryAttributes.DataDictionaryAttributesBuilder dataDictionaryAttributes();

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.Attribute} object.
     * The builder can be used to set properties and then create / update an instance of Attribute.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.Attribute.AttributeBuilder} the attribute builder
     */
    Attribute.AttributeBuilder attribute();
}
