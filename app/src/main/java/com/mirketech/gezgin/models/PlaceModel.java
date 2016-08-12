package com.mirketech.gezgin.models;

import android.graphics.Bitmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by yasin.avci on 23.5.2016.
 */
public class PlaceModel implements ClusterItem {

    private String _title;
    private String _desc;
    private LatLng _position;
    private int _order;
    private String _placeId;
    private Marker _marker;
    private Bitmap _bmpMarker;


    public String GetTitle() {
        return this._title;
    }

    public String GetDesc() {
        return this._desc;
    }

    public int GetOrder() {
        return this._order;
    }

    public String GetPlaceId() {
        return this._placeId;
    }

    public Marker GetMarker() {
        return this._marker;
    }

    public Bitmap GetBitmapMarker() {
        return this._bmpMarker;
    }


    public void SetTitle(String title) {
        _title = title;
    }

    public void SetDesc(String desc) {
        _desc = desc;
    }

    public void SetPosition(LatLng position) {
        _position = position;
    }

    public void SetOrder(int order) {
        _order = order;
    }

    public void SetPlaceId(String placeid) {
        _placeId = placeid;
    }

    public void SetMarker(Marker marker) {
        _marker = marker;
    }

    public void SetBitmapMarker(Bitmap bmp) {
        _bmpMarker = bmp;
    }


    @Override
    public LatLng getPosition() {
        return this._position;
    }
}
