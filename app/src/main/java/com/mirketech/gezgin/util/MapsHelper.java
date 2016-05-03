package com.mirketech.gezgin.util;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Created by yasin.avci on 3.5.2016.
 */
public class MapsHelper {

    public static void moveCamera(GoogleMap googleMap ,LatLng location){
        if(googleMap != null){
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(location, AppSettings.CAMERA_DEFAULT_MY_LOCATION_ZOOM_LEVEL);
            googleMap.animateCamera(update, AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);
        }

    }

    public static void moveCamera(GoogleMap googleMap ,LatLngBounds bounds){
        if(googleMap != null){
            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, AppSettings.CAMERA_DEFAULT_DIRECTION_PADDING_INPX);
            googleMap.animateCamera(update, AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);
        }

    }
}
