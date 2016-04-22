package com.mirketech.gezgin.comm;

/**
 * Created by yasin.avci on 22.4.2016.
 */
public class GResponse {

    public static final String SOURCE_DIRECTION         = "G_DIRECTION";
    public static final String SOURCE_PLACES            = "G_PLACES";

    public enum ResponseTypes{
        Success,
        Error,
        Log
    }



    public Object Data;
    public String Source;
    public ResponseTypes ResponseType;

    public GResponse(String _source , Object _data , ResponseTypes _type){
        Source = _source;
        Data = _data;
        ResponseType = _type;
    }


}
