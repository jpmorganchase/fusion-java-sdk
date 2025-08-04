package io.github.jpmorganchase.fusion.model;

import static io.github.jpmorganchase.fusion.model.VarArgsHelper.copyMap;

import com.google.gson.annotations.Expose;
import io.github.jpmorganchase.fusion.Fusion;
import java.util.LinkedHashSet;
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
public abstract class Resource {

    private final Map<String, Object> varArgs;

    @Expose(serialize = false, deserialize = false)
    private final Fusion fusion;

    public Resource(Map<String, Object> varArgs, Fusion fusion) {
        this.varArgs = copyMap(varArgs);
        this.fusion = fusion;
    }

    public Map<String, Object> getVarArgs() {
        return copyMap(varArgs);
    }

    /**
     * Returns the registered attributes pertaining to this resource.
     * A 'registered' attribute is essentially member variables belonging to the class.
     * It is expected that child classes will override, calling super to initialise and register attributes
     * @return set of attributes registered against this Resource
     */
    public Set<String> getRegisteredAttributes() {
        return new LinkedHashSet<>();
    }
}
