package io.github.jpmorganchase.fusion.model;

import io.github.jpmorganchase.fusion.Fusion;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * superclass of all entities contained in a catalog
 */
@Getter
@ToString
@EqualsAndHashCode
public abstract class CatalogResource extends Resource {

    private final String identifier;

    private final String catalogIdentifier;

    public CatalogResource(String identifier, Map<String, Object> varArgs, Fusion fusion, String catalogIdentifier) {
        super(varArgs, fusion);
        this.identifier = identifier;
        this.catalogIdentifier = catalogIdentifier;
    }

    protected String getCatalogIdentifier() {
        Fusion fusion = getFusion();
        if (catalogIdentifier == null && fusion != null) {
            return fusion.getDefaultCatalog();
        }
        return catalogIdentifier;
    }

    public String create() {
        return this.getFusion().create(getApiPath(), this);
    }

    public String update() {
        return this.getFusion().update(getApiPath(), this);
    }

    public String delete() {
        return this.getFusion().delete(getApiPath());
    }

    /**
     * Returns the API path used to perform operations on this catalog resource.
     * This path is utilized for creating, reading, updating, and deleting the resource.
     *
     * @return the API path for CRUD operations
     */
    protected abstract String getApiPath();

    /**
     * Returns the registered attributes pertaining to this catalog resource.
     * A 'registered' attribute is essentially member variables belonging to the class.
     * It is expected that child classes will override, calling super to initialise and register attributes
     * @return set of attributes registered against this CatalogResource
     */
    public Set<String> getRegisteredAttributes() {
        Set<String> registered = super.getRegisteredAttributes();
        registered.addAll(VarArgsHelper.getFieldNames(CatalogResource.class));
        registered.addAll(Arrays.asList("@id", "@context", "@base"));
        return registered;
    }
}
