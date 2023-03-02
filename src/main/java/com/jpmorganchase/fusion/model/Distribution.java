package com.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

/**
 * A distribution is a downloadable form of the data.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Distribution extends CatalogResource {

    String description;
    @SerializedName(value = "@id")
    String linkedEntity;
    String title;
    String fileExtension;
    String mediaType;

    @Builder
    public Distribution(String identifier, Map<String, String> varArgs, String description, String linkedEntity, String title, String fileExtension, String mediaType) {
        super(identifier, varArgs);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.fileExtension = fileExtension;
        this.mediaType = mediaType;
    }
}
