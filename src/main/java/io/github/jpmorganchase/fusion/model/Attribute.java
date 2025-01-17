package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.Fusion;
import java.util.Map;
import java.util.Set;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Attribute extends CatalogResource {

    @SerializedName("isDatasetKey")
    boolean key;

    String dataType;
    long index;
    String description;
    String title;

    @Expose(serialize = false, deserialize = false)
    String datasetIdentifier;

    boolean isCriticalDataElement;

    @Builder(toBuilder = true)
    public Attribute(
            @Builder.ObtainVia(method = "getIdentifier") String identifier,
            @Builder.ObtainVia(method = "getVarArgs") Map<String, Object> varArgs,
            @Builder.ObtainVia(method = "getFusion") Fusion fusion,
            @Builder.ObtainVia(method = "getCatalogIdentifier") String catalogIdentifier,
            boolean key,
            String dataType,
            long index,
            String description,
            String title,
            String datasetIdentifier,
            boolean isCriticalDataElement) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.key = key;
        this.dataType = dataType;
        this.index = index;
        this.description = description;
        this.title = title;
        this.datasetIdentifier = datasetIdentifier;
        this.isCriticalDataElement = isCriticalDataElement;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s/attributes/%4s",
                getFusion().getRootURL(), getCatalogIdentifier(), this.getDatasetIdentifier(), this.getIdentifier());
    }

    private String getApiPathForRegistration() {
        return getApiPath() + "/registration";
    }

    /**
     * Registers an attribute as a critical data element with the Fusion API.
     * <p>
     * This method sends a POST request to the Fusion API using the {@code create} method,
     * specifying that the attribute being registered is a critical data element.
     * </p>
     */
    public void register() {
        getFusion()
                .create(
                        getApiPathForRegistration(),
                        AttributeRegistration.builder()
                                .isCriticalDataElement(true)
                                .build());
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(Attribute.class));
        return exclusions;
    }

    public static class AttributeBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public Attribute.AttributeBuilder varArg(String key, Object value) {
            this.varArgs = VarArgsHelper.varArg(key, value, this.varArgs);
            return this;
        }

        public Attribute.AttributeBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = VarArgsHelper.copyMap(varArgs);
            return this;
        }
    }
}
