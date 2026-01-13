package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * A distribution file represents a single file within a distribution.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DistributionFile extends CatalogResource {

    String description;
    String fileExtension;
    String mediaType;
    String title;

    @Builder
    public DistributionFile(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            String description,
            String fileExtension,
            String mediaType,
            String title) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.description = description;
        this.fileExtension = fileExtension;
        this.mediaType = mediaType;
        this.title = title;
    }

    @Override
    protected String getApiPath() {
        throw new UnsupportedOperationException("Operation not yet supported for DistributionFile");
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(DistributionFile.class));
        return exclusions;
    }

    public static class DistributionFileBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DistributionFileBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
