package io.github.jpmorganchase.fusion.model;

import com.google.gson.annotations.Expose;
import io.github.jpmorganchase.fusion.api.APIManager;
import java.util.HashMap;
import java.util.Map;
import lombok.*;

/**
 * superclass of all entities contained in a catalog
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract class CatalogResource {

    private final String identifier;

    private Map<String, Object> varArgs;

    @Expose(serialize = false, deserialize = false)
    private final APIManager apiManager;

    @Expose(serialize = false, deserialize = false)
    private final String rootUrl;

    @Expose(serialize = false, deserialize = false)
    @Getter
    private final String catalogIdentifier;

    public CatalogResource(
            String identifier,
            Map<String, Object> varArgs,
            APIManager apiManager,
            String rootUrl,
            String catalogIdentifier) {
        this.varArgs = copyMap(varArgs);
        this.identifier = identifier;
        this.apiManager = apiManager;
        this.rootUrl = rootUrl;
        this.catalogIdentifier = catalogIdentifier;
    }

    public String create() {
        return this.apiManager.callAPIToPost(getApiPath(), this);
    }

    public String update() {
        return this.apiManager.callAPIToPut(getApiPath(), this);
    }

    /**
     * Returns the API path used to perform operations on this catalog resource.
     * This path is utilized for creating, reading, updating, and deleting the resource.
     *
     * @return the API path for CRUD operations
     */
    protected abstract String getApiPath();

    public Map<String, Object> getVarArgs() {
        return copyMap(varArgs);
    }

    static Map<String, Object> copyMap(Map<String, Object> source) {
        Map<String, Object> target = null;
        if (source != null) {
            target = new HashMap<>(source);
        }
        return target;
    }

    static Map<String, Object> initializeMap() {
        return new HashMap<>();
    }
}
