package io.github.jpmorganchase.fusion.builders;

import io.github.jpmorganchase.fusion.model.*;

public interface Builders {

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.Dataset} object.
     * The builder can be used to set properties and then create / update an instance of Dataset.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.Dataset.DatasetBuilder} the dataset builder
     */
    Dataset.DatasetBuilder dataset();

    /**
     * Returns a builder for creating a {@link io.github.jpmorganchase.fusion.model.ReportObj} object.
     * The builder can be used to set properties and then create / update an instance of ReportObj.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.ReportObj.ReportObjBuilder} the report builder
     */
    ReportObj.ReportObjBuilder report();

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

    /**
     * Returns a builder for creating an {@link io.github.jpmorganchase.fusion.model.Attributes} object.
     * The builder enables the configuration of multiple {@code Attribute} objects and other properties
     * associated with the {@code Attributes} resource. It supports chaining calls for setting
     * properties and building an {@code Attributes} instance.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.Attributes.AttributesBuilder} an instance of the attributes builder
     */
    Attributes.AttributesBuilder attributes();

    /**
     * Returns a builder for creating an {@link io.github.jpmorganchase.fusion.model.AttributeLineages} object.
     * The builder facilitates the configuration of multiple {@code AttributeLineage} objects and other properties
     * associated with the {@code AttributeLineages} resource. It supports chaining calls for setting
     * properties and building an {@code AttributeLineages} instance.
     *
     * @return {@link io.github.jpmorganchase.fusion.model.AttributeLineages.AttributeLineagesBuilder} an instance of the attribute lineages builder
     */
    AttributeLineages.AttributeLineagesBuilder attributeLineages();
}
