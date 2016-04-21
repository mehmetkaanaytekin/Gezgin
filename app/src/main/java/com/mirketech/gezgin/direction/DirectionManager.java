package com.mirketech.gezgin.direction;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mirketech.gezgin.comm.VolleyManager;

/**
 * Created by yasin.avci on 21.4.2016.
 */
public class DirectionManager {

    private static final String TAG = DirectionManager.class.getSimpleName();

    private static final String ROOT_URL                = "https://maps.googleapis.com/maps/api/directions/";
    private static final String QSTRING_JSON            = "json?";

    private static final String PARAM_LANGUAGE          = "language";
    private static final String PARAM_ORIGIN            = "origin";
    private static final String PARAM_DESTINATION       = "destination";
    private static final String PARAM_APIKEY            = "key";


    private Context appContext;
    private static DirectionManager ourInstance = null;

    public static DirectionManager getInstance(Context _context) {
        if(ourInstance != null){
            return ourInstance;
        }else{
            ourInstance = new DirectionManager(_context);
        }

        return ourInstance;

    }

    private DirectionManager(Context _context) {
        appContext = _context;
    }



    public String GetDirections(LatLng origin , LatLng dest){


        VolleyManager vManager = VolleyManager.getInstance(appContext);
        RequestQueue queue = vManager.getRequestQueue();


        JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
                "https://maps.googleapis.com/maps/api/directions/json?language=tr&origin=41.0003186, 28.859703&destination=40.7606417, 29.7248319&key=AIzaSyDUGe7_WKd1Sq182Iwycghz1ZB3u0cqews",
                null,
                createMyReqSuccessListener(),
                createMyReqErrorListener());

        queue.add(myReq);





        return "";
    }
    private String PrepareDirectionURL(LatLng origin , LatLng dest){




     return "";
    }


    private Response.Listener<JSONObject> createMyReqSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                    Log.d(TAG,"response : "+ response);
            }
        };
    }

    private Response.ErrorListener createMyReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG,"error : "+ error.getMessage());
            }
        };
    }







}
