package com.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DatasetSeries extends CatalogResource {

    LocalDate fromDate;
    LocalDate toDate;
    LocalDate createdDate;

    @SerializedName(value = "@id")
    String linkedEntity;

    @Builder
    public DatasetSeries(
            String identifier,
            Map<String, String> varArgs,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate,
            String linkedEntity) {
        super(identifier, varArgs);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdDate = createdDate;
        this.linkedEntity = linkedEntity;
    }
}
