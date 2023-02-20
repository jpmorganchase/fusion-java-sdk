package com.jpmorganchase.fusion;

import java.util.Map;

/**
 * An object represening a Fusion catalog, which is a container and inventory of datasets.
 */
public class Catalog extends CatalogResource {

    private String description;
    private String linkedEntity;
    private String title;

    /**
     * Given a map of field names and values describing a Fusion attribute will return a new object
     * @param fields a map of field names and values
     * @return an Attribute object
     */
    public static Catalog factory(Map<String, String> fields){

        Catalog catalog = new Catalog(fields.remove("identifier"));
        catalog.setDescription(fields.remove("description"));
        catalog.setTitle(fields.remove("title"));
        catalog.setLinkedEntity(fields.remove("@id"));
        //Store any remaining fields.
        if (fields.size() > 0) {
            catalog.setVarArgs(fields);
        }

        return catalog;
    }

    /**
     * Construct a new catalog with a Fusion catalog identifier
     * @param anIdentifier a Fusion catalog identifier

     */
    public Catalog(String anIdentifier){
        super(anIdentifier);
    }


    /**
     * THe description may include the owner and purpose of a catalog
     * @return the catalog description
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Set the catalog description
     * @param description a catalog description, e.g. owner and purpose
     */
    public void setDescription(String description){
        this.description = description;
    }


    /**
     * Returns the next entity in the catalog hierarchy
     * @return a String that can be appended to the current API URL to get the next entity
     */
    public String getLinkedEntity(){
        return this.linkedEntity;
    }

    /**
     * Set the linked entity in the catalog hierarchy
     * @param linkedEntity is the next entity in the hierarchy
     */
    public void setLinkedEntity(String linkedEntity){
       this.linkedEntity = linkedEntity;
    }

    /**
     * Returns the catalog title - a short name
     * @return catalog title
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Set the catalog display title - a short name
     */
    public void setTitle(String title){
        this.title = title;
    }



    /**
     * Returns a formatted string representation of the object to print
     * @return a printable string for the object
     */
    public String toString(){
        return "identifier: " + getIdentifier() +
                "\ndescription: " + getDescription() +
                "\n@id: " + getLinkedEntity() +
                "\ntitle: " + getTitle();
    }

}
