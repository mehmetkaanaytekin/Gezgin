package com.mirketech.gezgin.direction;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.PolyUtil;
import com.mirketech.gezgin.R;
import com.mirketech.gezgin.comm.CommManager;
import com.mirketech.gezgin.comm.GResponse;
import com.mirketech.gezgin.comm.VolleyManager;
import com.mirketech.gezgin.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by yasin.avci on 21.4.2016.
 */
public class DirectionManager {

    private static final String TAG = DirectionManager.class.getSimpleName();

    private static final String ROOT_URL = "https://maps.googleapis.com/maps/api/directions/";
    private static final String QSTRING_JSON = "json?";

    private static final String PARAM_LANGUAGE = "language";
    private static final String PARAM_ORIGIN = "origin";
    private static final String PARAM_DESTINATION = "destination";
    private static final String PARAM_APIKEY = "key";
    private static final String PARAM_AVOID_TOLLS = "avoid=tolls";
    private static final String PARAM_AVOID_HIGHWAYS = "avoid=highways";
    private static final String PARAM_AVOID_FERRIES = "avoid=ferries";


    private Context appContext;
    private static DirectionManager ourInstance = null;

    public static synchronized DirectionManager getInstance(Context _context) {
        if (ourInstance != null) {
            return ourInstance;
        } else {
            ourInstance = new DirectionManager(_context);
        }

        return ourInstance;

    }

    private DirectionManager(Context _context) {
        appContext = _context;
    }


    public void GetDirections(LatLng origin, LatLng dest) {


        VolleyManager vManager = VolleyManager.getInstance(appContext);
        RequestQueue queue = vManager.getRequestQueue();


        JsonObjectRequest myReq = new JsonObjectRequest(Method.GET,
                PrepareDirectionURL(origin, dest),
                null,
                createReqSuccessListener(),
                createReqErrorListener());

        queue.add(myReq);

    }

    private String PrepareDirectionURL(LatLng origin, LatLng dest) {

        StringBuilder sbU = new StringBuilder();
        sbU.append(DirectionManager.ROOT_URL);
        sbU.append(DirectionManager.QSTRING_JSON);
        sbU.append(DirectionManager.PARAM_ORIGIN + "=" + origin.latitude + "," + origin.longitude);
        sbU.append("&" + DirectionManager.PARAM_DESTINATION + "=" + dest.latitude + "," + dest.longitude);

        if(AppSettings.ROUTE_AVOID_TOLLS){
            sbU.append("&" + PARAM_AVOID_TOLLS);
        }
        if(AppSettings.ROUTE_AVOID_HIGHWAYS){
            sbU.append("&" + PARAM_AVOID_HIGHWAYS);
        }
        if(AppSettings.ROUTE_AVOID_FERRIES){
            sbU.append("&" + PARAM_AVOID_FERRIES);
        }

        sbU.append("&" + DirectionManager.PARAM_LANGUAGE + "=" + AppSettings.LANGUAGE);
        sbU.append("&" + DirectionManager.PARAM_APIKEY + "=" + appContext.getString(R.string.google_directions_apikey));


        return sbU.toString();
    }

    public List<LatLng> ParseDirectionResponse(GResponse response){
        try{

            JSONObject data = (JSONObject) response.Data;

            JSONArray routeObject = data.getJSONArray("routes");
            JSONObject routes = routeObject.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");

            return PolyUtil.decode(encodedString);

        }catch (Exception e){
            e.printStackTrace();
            return null;

        }

    }


    private Response.Listener<JSONObject> createReqSuccessListener() {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response : " + response);

                GResponse gResp = new GResponse(GResponse.RequestTypes.Direction, response, GResponse.ResponseStatus.Success);
                CommManager.getInstance().TriggerResponse(gResp);

            }
        };
    }

    private Response.ErrorListener createReqErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error : " + error.getMessage());

                GResponse gResp = new GResponse(GResponse.RequestTypes.Direction, error, GResponse.ResponseStatus.Error);
                CommManager.getInstance().TriggerResponse(gResp);

            }
        };
    }




}
