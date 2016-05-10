package com.mirketech.gezgin.util;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by yasin.avci on 21.4.2016.
 */
public class AppSettings {

    public static String LANGUAGE                                   = "tr";
    public static int CAMERA_DEFAULT_ZOOM_LEVEL                     = 8;
    public static int CAMERA_DEFAULT_MY_LOCATION_ZOOM_LEVEL         = 12;
    public static int CAMERA_DEFAULT_ANIMATE_DURATION_MS            = 4000;
    public static int CAMERA_DEFAULT_DIRECTION_PADDING_INPX         = 100;

    public static LatLng MAP_DEFAULT_LOCATION                       = new LatLng(41.0003186, 28.859703);

    public static boolean ROUTE_AVOID_TOLLS                         = false;
    public static boolean ROUTE_AVOID_HIGHWAYS                      = false;
    public static boolean ROUTE_AVOID_FERRIES                       = false;




}
