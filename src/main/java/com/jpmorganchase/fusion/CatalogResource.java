package com.jpmorganchase.fusion;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

/**
 * superclass of all entities contained in a catalog
 */
public abstract class CatalogResource {

    private final String identifier;
    private Map<String, String> varArgs;

    /**
     * Extracts metadata attributes for a catalog resource and puts into a map
     * @param jsonString returned from an API call
     * @return a map containing the metadata attributes
     */
    public static Map<String, Map> metadataAttributes(String jsonString){

        Map<String,Map> table = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(jsonString);
            JSONArray resources = (JSONArray) json.get("resources");

            for (Object o : resources) {
                Map entry = (Map) o;
                String key = (String) entry.get("@id");
                if (key == null){
                    key = entry.get("id").toString();
                }
                Map<Object, Object> values = new HashMap<>();
                for (Object value : entry.entrySet()) {
                    Map.Entry pair = (Map.Entry) value;
                    values.put(pair.getKey(), pair.getValue());
                }
                table.put(key, values);
            }

        }catch(Exception ex){
            System.out.println(ex.getMessage());
        }

        return table;
    }

    /**
     * Constructor
     * @param identifier the unique identifier for a new catalog entity
     */
    public CatalogResource(String identifier){
        this.identifier = identifier;
    }

    /**
     * Get the unique identifier
     * @return identifier
     */
    public String getIdentifier(){
        return this.identifier;
    }

    /**
     * Returns non-core attributes, handles new metadata that may be added in time.
     */
    public Map<String, String> getVarArgs(){
        return this.varArgs;
    }

    /**
     * Adds non-core attributes, handles new metadata that may be added in time.
     */
    public void setVarArgs(Map<String, String> varArgs){
        this.varArgs = varArgs;
    }
}
