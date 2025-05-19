package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportBusinessTerm extends Resource {

    String attributeId;
    String termId;

    @Builder(toBuilder = true)
    public ReportBusinessTerm(
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            String attributeId,
            String termId) {
        super(varArgs, fusion);
        this.attributeId = attributeId;
        this.termId = termId;
    }

    private String getAPIPath(String reportId) {
        return String.format(
                "%1s/api/corelineage-service/v1/reports/%2s/reportElements/businessTerms",
                getFusion().getNewRootURL(), reportId);
    }

    public String create(String reportId) {
        List<ReportBusinessTerm> businessTerms = new ArrayList<>();
        businessTerms.add(this);
        return getFusion().create(getAPIPath(reportId), businessTerms);
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class ReportBusinessTermBuilder {
        private Map<String, Object> varArgs;

        public ReportBusinessTerm.ReportBusinessTermBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public ReportBusinessTerm.ReportBusinessTermBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
