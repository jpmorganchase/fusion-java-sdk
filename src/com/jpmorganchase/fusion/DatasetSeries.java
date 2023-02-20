package com.jpmorganchase.fusion;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class DatasetSeries extends CatalogResource {

    private Date fromDate;
    private Date toDate;
    private Date createdDate;
    private String linkedEntity;


    /**
     * Given a map of field names and values describing a Fusion dataset series will return a new object
     * @param fields a map of field names and values
     * @return a DatasetSeries object
     */
    public static DatasetSeries factory(Map<String, String> fields) throws ParseException{

        DatasetSeries datasetSeries = new DatasetSeries(fields.remove("identifier"));
        datasetSeries.setFromDate(fields.remove("fromDate"));
        datasetSeries.setToDate(fields.remove("toDate"));
        datasetSeries.setCreatedDate(fields.remove("createdDate"));
        datasetSeries.setLinkedEntity(fields.remove("@id"));
        //Store any remaining fields.
        if (fields.size() > 0) {
            datasetSeries.setVarArgs(fields);
        }

        return datasetSeries;
    }

    /**
     * Construct a new DatasetSeries with a Fusion series identifier
     * @param anIdentifier a Fusion series identifier

     */
    public DatasetSeries(String anIdentifier){
        super(anIdentifier);
    }

    /**
     * Set the start date for data covered in a series member
     * @param fromDateStr the start date
     */
    public void setFromDate(String fromDateStr) throws ParseException{
        SimpleDateFormat dateParseFormat = new SimpleDateFormat("yyyyMMdd");
        this.fromDate = dateParseFormat.parse(fromDateStr);
    }

    /**
     * Return the start date for data in the series member
     * @return the series member start date
     */
    public Date getFromDate(){
        return this.fromDate;
    }

    /**
     * Set the latest date for data covered in a series member
     * @param toDateStr the end date
     */
    public void setToDate(String toDateStr) throws ParseException{
        SimpleDateFormat dateParseFormat = new SimpleDateFormat("yyyyMMdd");
        this.toDate = dateParseFormat.parse(toDateStr);
    }

    /**
     * Return the end date for data in the series member
     * @return the series member end date
     */
    public Date getToDate(){
        return this.toDate;
    }


    /**
     * Set the created date for the series member
     * @param createdDateStr the date when the series member was created
     */
    public void setCreatedDate(String createdDateStr) throws ParseException{
        SimpleDateFormat dateParseFormat = new SimpleDateFormat("yyyyMMdd");
        this.createdDate = dateParseFormat.parse(createdDateStr);
    }

    /**
     * Return the series member created date
     * @return the date the series member was created
     */
    public Date getCreatedDate(){
        return this.createdDate;
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
     * Returns a formatted string representation of the object to print
     * @return a string representation of the object
     */
    public String toString() {
        SimpleDateFormat dateParseFormat = new SimpleDateFormat("yyyyMMdd");
        return "\nidentifier: " + getIdentifier() +
                "\nfromDate: " + dateParseFormat.format(getFromDate()) +
                "\ntoDate: " + dateParseFormat.format(getToDate()) +
                "\ncreatedDate: " + dateParseFormat.format(getCreatedDate()) +
                "\n@id: " + getLinkedEntity();
    }

}
