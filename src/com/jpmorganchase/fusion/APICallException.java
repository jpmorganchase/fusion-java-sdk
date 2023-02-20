package com.jpmorganchase.fusion;

/**
 * A custom exception to provide useful information on the state of an API call
 */
public class APICallException extends Exception{

    private final int responseCode;


    public APICallException(int responseCode){
        this.responseCode = responseCode;
    }

    /**
     * Returns the HTTP response code
     * @return a response code
     */
    public int getResponseCode(){
        return this.responseCode;
    }

    /**
     * Get a meaningful response message
     * @return a description for the exception
     */
    public String getMessage(){

        String errorMsg = "Unknown";
         switch(this.responseCode){
             case 403:
                 errorMsg = "Not permitted. Check credentials are correct or you are subscribed to the dataset";
                 break;
             case 500:
                 errorMsg = "Internal API error";
                 break;

        }
        return errorMsg;
    }
}
