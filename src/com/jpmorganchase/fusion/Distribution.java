package com.jpmorganchase.fusion;

import java.util.Map;

/**
 * A distribution is a downloadable form of the data.
 */
public class Distribution extends CatalogResource {


    private String description;
    private String linkedEntity;
    private String title;
    private String fileExtension;
    private String mediaType;

    /**
     * Given a map of field names and values describing a Fusion attribute will return a new object
     * @param fields a map of field names and values
     * @return an Attribute object
     */
    public static Distribution factory(Map<String, String> fields){

        Distribution distribution = new Distribution(fields.remove("identifier"));
        distribution.setDescription(fields.remove("description"));
        distribution.setTitle(fields.remove("title"));
        distribution.setLinkedEntity(fields.remove("@id"));
        distribution.setFileExtension(fields.remove("fileExtension"));
        distribution.setMediaType(fields.remove("mediaType"));
        //Store any remaining fields.
        if (fields.size() > 0) {
            distribution.setVarArgs(fields);
        }

        return distribution;
    }

    /**
     * Construct a new catalog with a Fusion catalog identifier
     * @param anIdentifier a Fusion catalog identifier

     */
    public Distribution(String anIdentifier){
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
     */
    public void setDescription(String description){
        this.description = description;
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
     * Get the distribution title
     * @return a title
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Sets the distribution title
     * @param title the title text to apply to the distribution
     */
    public void setTitle(String title){
        this.title = title;
    }

    /**
     * Returns the file extension, e.g. .csv for a CSV file
     * @return a file extension
     */
    public String getFileExtension(){
        return this.fileExtension;
    }

    /**
     * Sets the file extension, e.g. .csv for a CSV file
     * @param fileExtension a valid file extension corresponding to the distribution type
     */
    public void setFileExtension(String fileExtension){
        this.fileExtension = fileExtension;
    }

    /**
     * Sets the distribution media type
     * @return a media type
     */
    public String getMediaType(){
        return this.mediaType;
    }

    /**
     * Sets the distribution media type
     * @param mediaType a media type
     */
    public void setMediaType(String mediaType){
        this.mediaType = mediaType;
    }


    /**
     * Returns a formatted string representation of the object to print
     * @return a String representation of the object
     */
    public String toString(){
        return "identifier: " + getIdentifier() +
                "\ndescription: " + getDescription() +
                "\n@id: " + getLinkedEntity() +
                "\ntitle: " + getTitle() +
                "\nfileExtension:" + getFileExtension() +
                "\nmediaType:" + getMediaType();
    }
}
