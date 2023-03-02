package com.jpmorganchase.fusion.model;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.HashMap;
import java.util.Map;

/**
 * superclass of all entities contained in a catalog
 */
@Getter
@AllArgsConstructor //TODO: Deep copy the map?
@ToString
@EqualsAndHashCode
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
}
