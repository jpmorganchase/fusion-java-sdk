package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
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
            Map<String, Object> varArgs,
            APIManager apiManager,
            String rootUrl,
            String catalogIdentifier,
            LocalDate fromDate,
            LocalDate toDate,
            LocalDate createdDate,
            String linkedEntity) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.createdDate = createdDate;
        this.linkedEntity = linkedEntity;
    }

    @Override
    protected String getApiPath() {
        throw new UnsupportedOperationException("Operation not yet supported for DatasetSeries");
    }

    public static class DatasetSeriesBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DatasetSeriesBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
