package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Report extends Resource {

    String id;
    String title;
    String description;
    String frequency;
    String category;
    String subCategory;
    boolean regulatoryRelated;
    Domain domain;
    DataNodeId dataNodeId;

    @Builder(toBuilder = true)
    public Report(
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            String id,
            String title,
            String description,
            String frequency,
            String category,
            String subCategory,
            boolean regulatoryRelated,
            Domain domain,
            DataNodeId dataNodeId) {
        super(varArgs, fusion);
        this.id = id;
        this.title = title;
        this.description = description;
        this.frequency = frequency;
        this.category = category;
        this.subCategory = subCategory;
        this.regulatoryRelated = regulatoryRelated;
        this.domain = domain;
        this.dataNodeId = dataNodeId;
    }

    private String getAPIPath() {
        return String.format(
                "%1s/api/corelineage-service/v1/reports", getFusion().getNewRootURL());
    }

    public String create() {
        return getFusion().create(getAPIPath(), this);
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(Report.class));
        return exclusions;
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class ReportBuilder {
        private Map<String, Object> varArgs;
        private DataNodeId dataNodeId;

        public Report.ReportBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public Report.ReportBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }

        public Report.ReportBuilder dataNodeIdValues(String id, String name, String type) {
            this.dataNodeId = new DataNodeId(id, name, type);
            return this;
        }
    }
}
