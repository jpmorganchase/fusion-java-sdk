package com.jpmorganchase.fusion;

import java.util.Map;

/**
 * An object representing a data product. Object properties hold metadata attributes and descriptions.
 */
public class DataProduct extends CatalogResource {

    private String description;
    private String linkedEntity;
    private String title;
    private String status;

    /**
     * Given a map of field names and values describing a Fusion data product will return a new object
     * @param fields a map of field names and values
     * @return an DataProduct object
     */
    public static DataProduct factory(Map<String, String> fields){

        DataProduct dataProduct = new DataProduct(fields.remove("identifier"));
        dataProduct.setDescription(fields.remove("description"));
        dataProduct.setTitle(fields.remove("title"));
        dataProduct.setStatus(fields.remove("status"));
        dataProduct.setLinkedEntity(fields.remove("@id"));

        //Store any remaining fields.
        if (fields.size() > 0) {
            dataProduct.setVarArgs(fields);
        }

        return dataProduct;
    }

    /**
     * Construct a new data product object with core attributes and other attributes in a map. THis is
     * designed to support the addition of additional metadata in the future.
     * @param anIdentifier a unique Fusion data product identifier
     */
    public DataProduct(String anIdentifier){
        super(anIdentifier);
    }


    /**
     * Returns the data product description. Typically this describes the datasets in the product
     */
    public String getDescription(){
        return this.description;
    }

    /**
     * Set the data product description
     * @param description a data product description, e.g. the datasets in the product
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
     * Returns the status of the data product
     */
    public String getStatus(){
        return this.status;
    }

    /**
     * Set the data product status
     * @param status is the next entity in the hierarchy
     */
    public void setStatus(String status){
        this.status = status;
    }

    /**
     * Returns the data product title
     * @return product title
     */
    public String getTitle(){
        return this.title;
    }

    /**
     * Set the data product display title - a short name
     */
    public void setTitle(String title){
        this.title = title;
    }


    /**
     * Returns a printable description of the data product.
     */
    public String toString(){
        return "identifier: " + getIdentifier() +
                "\ndescription: " + getDescription() +
                "\n@id: " + getLinkedEntity() +
                "\ntitle: " + getTitle() +
                "\nstatus: " + getStatus();
    }
}
