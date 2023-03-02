package com.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DatasetSeries extends CatalogResource {

    Date fromDate;
    Date toDate;
    Date createdDate;
    @SerializedName(value = "@id")
    String linkedEntity;

    @Builder
    public DatasetSeries(String identifier, Map<String, String> varArgs, Date fromDate, Date toDate, Date createdDate, String linkedEntity) {
        super(identifier, varArgs);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdDate = createdDate;
        this.linkedEntity = linkedEntity;
    }
}
