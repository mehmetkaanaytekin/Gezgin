package com.mirketech.gezgin.models;

/**
 * Created by yusema on 01/05/16.
 */
public class SuggestModel {

    private double _latitude;
    private double _longitude;
    private String _name;
    private String _description;



    public String getName(){
        return this._name;
    }

    public void setName(String name){
        this._name = name;
    }

    public String getDescription(){
        return this._description;
    }

    public void setDescription(String desc){
        this._description = desc;
    }

    public double getLatitude(){
        return this._latitude;
    }

    public void setLatitude(double lat){
        this._latitude = lat;
    }

    public double getLongitude(){
        return this._longitude;
    }

    public void setLongitude(double lng){
        this._longitude = lng;
    }



}
