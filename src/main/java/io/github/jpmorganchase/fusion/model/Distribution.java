package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.Map;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;

/**
 * A distribution is a downloadable form of the data.
 */
@Value
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Distribution extends CatalogResource {

    String description;

    @SerializedName(value = "@id")
    String linkedEntity;

    String title;
    String fileExtension;
    String mediaType;

    @Builder
    public Distribution(
            String identifier,
            Map<String, Object> varArgs,
            APIManager apiManager,
            String rootUrl,
            String catalogIdentifier,
            String description,
            String linkedEntity,
            String title,
            String fileExtension,
            String mediaType) {
        super(identifier, varArgs, apiManager, rootUrl, catalogIdentifier);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.fileExtension = fileExtension;
        this.mediaType = mediaType;
    }

    @Override
    protected String getApiPath() {
        throw new UnsupportedOperationException("Operation not yet supported for Distribution");
    }

    public static class DistributionBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, Object> varArgs;

        public DistributionBuilder varArgs(Map<String, Object> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
