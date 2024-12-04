package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.Map;
import java.util.Optional;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * An object representing a dataset. Object properties hold dataset metadata attributes
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Dataset extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;
    String frequency;
    String type;
    Report report;
    Application applicationId;
    Application producerApplicationId;
    Application[] consumerApplicationId;
    Flow flowDetails;

    @Builder(toBuilder = true)
    public Dataset(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getApiManager") APIManager apiManager,
            @Builder.ObtainVia(method = "getRootUrl") String rootUrl,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String description,
            String linkedEntity,
            String title,
            String frequency,
            String type,
            Report report,
            Application applicationId,
            Application producerApplicationId,
            Application[] consumerApplicationId,
            Flow flowDetails) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.frequency = frequency;
        this.type = type;
        this.report = report;
        this.applicationId = applicationId;
        this.producerApplicationId = producerApplicationId;
        this.consumerApplicationId = consumerApplicationId;
        this.flowDetails = flowDetails;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s", this.getRootUrl(), this.getCatalogIdentifier(), this.getIdentifier());
    }

    public static class DatasetBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DatasetBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DatasetBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public DatasetBuilder report(Report report) {
            Optional.ofNullable(report).ifPresent(val -> {
                this.type = "Report";
                this.report = val;
            });
            return this;
        }

        public DatasetBuilder inputFlow(Application producerApplicationId, Application[] consumerApplicationId) {
            Optional.ofNullable(producerApplicationId).ifPresent(val -> {
                this.type = "Flow";
                this.flowDetails = Flow.builder().flowDirection("Input").build();
                this.producerApplicationId = producerApplicationId;
                this.consumerApplicationId = consumerApplicationId;
            });
            return this;
        }

        public DatasetBuilder outputFlow(Application producerApplicationId, Application[] consumerApplicationId) {
            Optional.ofNullable(producerApplicationId).ifPresent(val -> {
                this.type = "Flow";
                this.flowDetails = Flow.builder().flowDirection("Output").build();
                this.producerApplicationId = producerApplicationId;
                this.consumerApplicationId = consumerApplicationId;
            });
            return this;
        }
    }
}
