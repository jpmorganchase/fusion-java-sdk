package com.jpmorganchase.fusion;

import java.util.Map;


/**
 * An object representing a dataset. Object properties hold dataset metadata attributes
 */
public class Dataset extends CatalogResource {

    private String description;
    private String linkedEntity;
    private String title;
    private String frequency;

    /**
     * Create a new dataset object from a map of metadata attributes and values
     * @param fields a map of metadata attributes and descriptive information for a dataset
     * @return an Dataset object
     */
    public static Dataset factory(Map<String, String> fields) {

        Dataset dataset = new Dataset(fields.remove("identifier"));
        dataset.setDescription(fields.remove("description"));
        dataset.setTitle(fields.remove("title"));
        dataset.setFrequency(fields.remove("frequency"));
        dataset.setLinkedEntity(fields.remove("@id"));
        //Store any remaining fields.
        if (fields.size() > 0) {
            dataset.setVarArgs(fields);
        }

        return dataset;
    }


    /**
     * Construct a new dataset object with core attributes and store any other attributes in a map. THis is
     * designed to support the addition of additional metadata in the future.
     * @param anIdentifier a unique Fusion dataset identifier
     */
    public Dataset(String anIdentifier){
        super(anIdentifier);
    }


    /**
     * Set the dataset description
     * @param description a dataset description, e.g. describing the data in the dataset
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Returns the dataset publication frequency, e.g. daily
     */
    public String getFrequency(){
        return this.frequency;
    }

    /**
     * Set the dataset frequency
     * @param frequency a dataset publication frequency, e.g. daily
     */
    public void setFrequency(String frequency){
        this.frequency = frequency;
    }

    /**
     * Returns the dataset description. Typically describing the data in the dataset
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Returns the next level in the API URL
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
     * Returns the dataset title
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
     * Returns a printable description of the catalog.
     */
    public String toString(){
        return "identifier: " + getIdentifier() +
                "\ndescription: " + getDescription() +
                "\n@id: " + getLinkedEntity() +
                "\ntitle: " + getTitle() +
                "\nfrequency: " + getFrequency();
    }
}
