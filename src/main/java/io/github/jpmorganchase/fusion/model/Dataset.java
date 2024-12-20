package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.Fusion;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    List<Application> consumerApplicationId;
    Flow flowDetails;
    String publisher;

    @Builder(toBuilder = true)
    public Dataset(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String description,
            String linkedEntity,
            String title,
            String frequency,
            String type,
            Report report,
            Application applicationId,
            Application producerApplicationId,
            List<Application> consumerApplicationId,
            Flow flowDetails,
            String publisher) {
        super(identifier, varArgs, fusion, catalogIdentifier);
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
        this.publisher = publisher;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s",
                getFusion().getRootURL(), this.getCatalogIdentifier(), this.getIdentifier());
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

            if (Objects.nonNull(report) && Objects.nonNull(report.getTier())) {
                this.type = "Report";
                this.report = report;
            }
            return this;
        }

        public DatasetBuilder flow(Flow flow) {
            this.type = "Flow";

            this.producerApplicationId = flow.getProducerApplicationId();
            this.consumerApplicationId = flow.getConsumerApplicationId();
            this.flowDetails = flow;
            return this;
        }
    }
}
