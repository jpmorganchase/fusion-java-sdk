package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportObj extends Dataset {

    ReportDetail report;
    String tier;

    @Builder(toBuilder = true)
    public ReportObj(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            @Builder.ObtainVia(method = "getDescription") String description,
            @Builder.ObtainVia(method = "getLinkedEntity") String linkedEntity,
            @Builder.ObtainVia(method = "getTitle") String title,
            @Builder.ObtainVia(method = "getFrequency") String frequency,
            @Builder.ObtainVia(method = "getApplicationId") Application applicationId,
            @Builder.ObtainVia(method = "getProducerApplicationId") Application producerApplicationId,
            @Builder.ObtainVia(method = "getConsumerApplicationId") List<Application> consumerApplicationId,
            @Builder.ObtainVia(method = "getFlowDetails") Flow flowDetails,
            @Builder.ObtainVia(method = "getPublisher") String publisher,
            ReportDetail report,
            String tier) {
        super(
                identifier,
                varArgs,
                fusion,
                catalogIdentifier,
                description,
                linkedEntity,
                title,
                frequency,
                "Report",
                applicationId,
                producerApplicationId,
                consumerApplicationId,
                flowDetails,
                publisher);
        this.report = report;
        this.tier = tier;
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        return VarArgsHelper.getFieldNames(exclusions, ReportObj.class);
    }

    public static class ReportObjBuilder extends DatasetBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        private ReportObj.ReportObjBuilder report(ReportDetail report) {
            return this;
        }

        public ReportObj.ReportObjBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public ReportObj.ReportObjBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public ReportObj.ReportObjBuilder tier(String tier) {
            this.tier = tier;
            if (null != tier && !tier.isEmpty()) {
                this.report = ReportDetail.builder().tier(tier).build();
            }
            return this;
        }
    }
}
