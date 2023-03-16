package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.SerializedName;
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
            Map<String, String> varArgs,
            String description,
            String linkedEntity,
            String title,
            String fileExtension,
            String mediaType) {
        super(identifier, varArgs);
        this.description = description;
        this.linkedEntity = linkedEntity;
        this.title = title;
        this.fileExtension = fileExtension;
        this.mediaType = mediaType;
    }

    public static class DistributionBuilder {
        @SuppressWarnings("FieldCanBeLocal")
        private Map<String, String> varArgs;

        public DistributionBuilder varArgs(Map<String, String> varArgs) {
            this.varArgs = copyMap(varArgs);
            return this;
        }
    }
}
