package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.ArrayList;
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
public class ReportAttribute extends Resource {

    String id;
    String title;

    @Builder(toBuilder = true)
    public ReportAttribute(
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            String id,
            String title) {
        super(varArgs, fusion);
        this.id = id;
        this.title = title;
    }

    private String getAPIPath(String reportId) {
        return String.format(
                "%1s/api/corelineage-service/v1/reports/%2s/reportElements",
                getFusion().getNewRootURL(), reportId);
    }

    public String create(String reportId) {
        List<ReportAttribute> attributes = new ArrayList<>();
        attributes.add(this);
        return getFusion().create(getAPIPath(reportId), attributes);
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(ReportAttribute.class));
        return exclusions;
    }

    @SuppressWarnings("FieldCanBeLocal")
    public static class ReportAttributeBuilder {
        private Map<String, Object> varArgs;

        public ReportAttribute.ReportAttributeBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public ReportAttribute.ReportAttributeBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
