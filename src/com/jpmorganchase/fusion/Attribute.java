package com.jpmorganchase.fusion;

import java.util.Map;

public class Attribute extends CatalogResource{

    private boolean isKey;
    private String datatype;
    private long index;
    private String description;
    private String title;

    /**
     * Given a map of field names and values describing a Fusion attribute will return a new object
     * @param fields a map of field names and values
     * @return an Attribute object
     */
    public static Attribute factory(Map<String, String> fields){

        Attribute attribute = new Attribute(fields.remove("identifier"));
        //attribute.setIsKey(fields.remove("isDatasetKey"));
        attribute.setDatatype(fields.remove("dataType"));
        //attribute.setIndex(fields.remove("index"));
        attribute.setDescription(fields.remove("description"));
        attribute.setTitle(fields.remove("title"));

        //Store any remaining fields.
        if (fields.size() > 0) {
            attribute.setVarArgs(fields);
        }
        return attribute;
    }

    /**
     * Constructor to return a new attribute object
     * @param identifier an attribute identifier
     */
    public Attribute(String identifier){

        super(identifier);
    }


    /**
     * Sets the isKey which is true if the attribute is a primary key for the dataset
     * @param key a boolean value
     */
    public void setIsKey(String key){
        this.isKey = Boolean.parseBoolean(key.toLowerCase());
    }

    /**
     * Returns true if the attribute is a key, false otherwise
     * @return attribute key status
     */
    public boolean isKey(){

        return this.isKey;
    }

    /**
     * Sets the data type of the Attribute
     * @param datatype a datatype, e.g. String, Integer, Date etc
     */
    public void setDatatype(String datatype){
        this.datatype = datatype;
    }

    /**
     * Returns the attribute data type
     * @return data type of the attribute
     */
    public String getDatatype(){

        return this.datatype;
    }

    /**
     * Set the attribute index, this is its position in the dataset
     * @param index of the attribute starting at 0
     */
    public void setIndex(String index){
        this.index = Long.parseLong(index);
    }

    /**
     * Returns the attribute index, first column in dataset has index 0
     * @return an attribute index
     */
    public long getIndex(){

        return this.index;
    }

    /**
     * Sets the description of the attribute
     * @param description an attribute description
     */
    public void setDescription(String description){
        this.description = description;
    }

    /**
     * Returns the attribute description
     * @return attribute description
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Sets the display title of the attribute
     * @param title an attribute title
     */
    public void setTitle(String title){

        this.title = title;
    }

    /**
     * Returns the attribute display title
     * @return attribute title
     */
    public String getTitle(){
        return this.title;
    }


    /**
     * Returns a formatted string representation of the object to print
     * @return a printable string for the object
     */
    public String toString(){
        return "identifier: " + getIdentifier() +
                "\ndescription: " + getDescription() +
                "\ntitle: " + getTitle()+
                "\ndata type: " + getDatatype();
                //"\nindex: " + getIndex() +
                //"\nis key: + " + isKey();
    }

}
