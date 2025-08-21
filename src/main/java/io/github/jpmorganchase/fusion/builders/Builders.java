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

    //    Report.ReportBuilder report(); TODO Report

    /**
     * Returns a builder for creating a {@link DataFlow} object.
     * The builder can be used to set properties and then create / update an instance of ReportObj.
     *
     * @return {@link DataFlow.DataFlowBuilder} the report builder
     */
    DataFlow.DataFlowBuilder dataFlow();

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
