package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.Fusion;
import java.util.*;
import lombok.*;

/**
 * An object representing a dataset. Object properties hold dataset metadata attributes
 */
@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Dataset extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;
    String frequency;
    String type;
    Application applicationId;
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
            Application applicationId,
            String publisher) {
        super(identifier, varArgs, fusion, catalogIdentifier);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.frequency = frequency;
        this.type = type;
        this.applicationId = applicationId;
        this.publisher = publisher;
    }

    @Override
    protected String getApiPath() {
        return String.format(
                "%1scatalogs/%2s/datasets/%3s",
                getFusion().getRootURL(), this.getCatalogIdentifier(), this.getIdentifier());
    }

    protected String getApiPathForLineage() {
        return getApiPath() + "/lineage";
    }

    /**
     * Creates a lineage in the Fusion API.
     * <p>
     * This method sends a POST request to the Fusion API to register the provided
     * {@code SourceDatasets} object, establishing lineage information for a dataset.
     * </p>
     *
     * @param lineage the {@code SourceDatasets} object representing the dataset lineage to be created
     */
    public void createLineage(SourceDatasets lineage) {
        getFusion().create(getApiPathForLineage(), lineage);
    }

    /**
     * Retrieves the lineage information for the current dataset from the Fusion API.
     * <p>
     * This method sends a request to the Fusion API to fetch the lineage details
     * associated with the dataset identified by the catalog identifier and dataset identifier.
     * </p>
     *
     * @return the {@code DatasetLineage} object containing lineage details for the dataset
     */
    public DatasetLineage getLineage() {
        return getFusion().getLineage(this.getCatalogIdentifier(), this.getIdentifier());
    }

    @Override
    public Set<String> getRegisteredAttributes() {
        Set<String> exclusions = super.getRegisteredAttributes();
        exclusions.addAll(VarArgsHelper.getFieldNames(Dataset.class));
        return exclusions;
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
    }
}
