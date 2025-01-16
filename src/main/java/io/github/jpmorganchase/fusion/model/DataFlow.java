package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Collections;
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
public class DataFlow extends Dataset {

    Application producerApplicationId;
    List<Application> consumerApplicationId;
    Flow flowDetails;

    @Builder(toBuilder = true)
    public DataFlow(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            @Builder.ObtainVia(method = "getDescription") String description,
            @Builder.ObtainVia(method = "getLinkedEntity") String linkedEntity,
            @Builder.ObtainVia(method = "getTitle") String title,
            @Builder.ObtainVia(method = "getFrequency") String frequency,
            @Builder.ObtainVia(method = "getType") String type,
            @Builder.ObtainVia(method = "getApplicationId") Application applicationId,
            @Builder.ObtainVia(method = "getPublisher") String publisher,
            Application producerApplicationId,
            List<Application> consumerApplicationId,
            Flow flowDetails) {
        super(
                identifier,
                varArgs,
                fusion,
                catalogIdentifier,
                description,
                linkedEntity,
                title,
                frequency,
                type,
                applicationId,
                publisher);
        this.producerApplicationId = producerApplicationId;
        this.consumerApplicationId =
                consumerApplicationId != null ? Collections.unmodifiableList(consumerApplicationId) : null;
        this.flowDetails = flowDetails;
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(DataFlow.class));
        return exclusions;
    }

    public static class DataFlowBuilder extends DatasetBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DataFlow.DataFlowBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public DataFlow.DataFlowBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public DataFlow.DataFlowBuilder flow(Flow flowDetails) {
            this.type = DatasetType.FLOW.getLabel();
            if (null != flowDetails) {
                this.producerApplicationId = flowDetails.getProducerApplicationId();
                this.consumerApplicationId = flowDetails.getConsumerApplicationId();
                this.flowDetails = flowDetails;
            }
            return this;
        }
    }
}
