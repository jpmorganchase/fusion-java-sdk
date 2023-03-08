package com.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Attribute extends CatalogResource{

    @SerializedName("isDatasetKey")
    boolean key;
    String dataType;
    long index;
    String description;
    String title;

    @Builder
    public Attribute(String identifier, Map<String, String> varArgs, boolean key, String dataType, long index, String description, String title) {
        super(identifier, varArgs);
        this.key = key;
        this.dataType = dataType;
        this.index = index;
        this.description = description;
        this.title = title;
    }
}
