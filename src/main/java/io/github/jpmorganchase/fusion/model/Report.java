package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Report extends Resource {

    String name;
    String tierType;
    String lob;
    DataNodeId dataNodeId;
    AlternativeId alternativeId;

    @Builder(toBuilder = true)
    public Report(
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            String name,
            String tierType,
            String lob,
            DataNodeId dataNodeId,
            AlternativeId alternativeId) {
        super(varArgs, fusion);
        this.name = name;
        this.tierType = tierType;
        this.lob = lob;
        this.dataNodeId = dataNodeId;
        this.alternativeId = alternativeId;
    }

    private String getAPIPath() {
        return String.format(
                "%1s/api/corelineage-service/v1/reports", getFusion().getNewRootURL());
    }

    public String create() {
        return getFusion().create(getAPIPath(), this);
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class ReportBuilder {
        private Map<String, Object> varArgs;
        private DataNodeId dataNodeId;
        private AlternativeId alternativeId;

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

        public Report.ReportBuilder alternativeIdValues(Domain domain, String value) {
            this.alternativeId = new AlternativeId(domain, value);
            return this;
        }
    }
}
