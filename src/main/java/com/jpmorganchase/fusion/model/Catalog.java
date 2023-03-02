package com.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

/**
 * An object represening a Fusion catalog, which is a container and inventory of datasets.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Catalog extends CatalogResource {

    String description;
    @SerializedName(value = "@id")
    String linkedEntity;
    String title;

    @Builder
    public Catalog(String identifier, Map<String, String> varArgs, String description, String linkedEntity, String title) {
        super(identifier, varArgs);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
    }
}
