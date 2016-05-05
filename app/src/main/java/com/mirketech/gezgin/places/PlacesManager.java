package com.mirketech.gezgin.places;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.mirketech.gezgin.R;
import com.mirketech.gezgin.comm.CommManager;
import com.mirketech.gezgin.comm.GResponse;
import com.mirketech.gezgin.comm.VolleyManager;
import com.mirketech.gezgin.util.AppSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by yasin.avci on 26.4.2016.
 */
public class PlacesManager {
    private static final String TAG = PlacesManager.class.getSimpleName();

    private static final String AUTOCOMPLETE_ROOT_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/";
    private static final String DETAILS_ROOT_URL = "https://maps.googleapis.com/maps/api/place/details/";
    private static final String QSTRING_JSON = "json?";
    private static final String PARAM_INPUT = "input";
    private static final String PARAM_PLACEID = "placeid";
    private static final String PARAM_APIKEY = "key";
    private static final String PARAM_LANGUAGE = "language";

    private Context appContext;
    private static PlacesManager ourInstance = null;

    public static synchronized PlacesManager getInstance(Context _context) {
        if (ourInstance != null) {
            return ourInstance;
        } else {
            ourInstance = new PlacesManager(_context);
        }

        return ourInstance;

    }

    private PlacesManager(Context _context) {
        appContext = _context;
    }


    public void GetPlacesAutoComplete(String input) {

        VolleyManager vManager = VolleyManager.getInstance(appContext);
        RequestQueue queue = vManager.getRequestQueue();


        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST,
                PreparePlacesAutoCompleteURL(input),
                null,
                createReqSuccessListener(GResponse.RequestTypes.Places_Autocomplete),
                createReqErrorListener(GResponse.RequestTypes.Places_Autocomplete));

        queue.add(myReq);


    }

    public void GetPlaceDetails(String place_id){

        VolleyManager vManager = VolleyManager.getInstance(appContext);
        RequestQueue queue = vManager.getRequestQueue();
        GResponse.RequestTypes reqType = GResponse.RequestTypes.Places_GetDetails;

        JsonObjectRequest myReq = new JsonObjectRequest(Request.Method.POST,
                PreparePlacesDetailsURL(place_id),
                null,
                createReqSuccessListener(reqType),
                createReqErrorListener(reqType));

        queue.add(myReq);

    }

    public String PreparePlacesAutoCompleteURL(String input) {

        StringBuilder sbU = new StringBuilder();
        sbU.append(PlacesManager.AUTOCOMPLETE_ROOT_URL);
        sbU.append(PlacesManager.QSTRING_JSON);
        sbU.append(PlacesManager.PARAM_INPUT + "=" + input);
        sbU.append("&" + PlacesManager.PARAM_LANGUAGE + "=" + AppSettings.LANGUAGE);
        sbU.append("&" + PlacesManager.PARAM_APIKEY + "=" + appContext.getString(R.string.google_directions_apikey));

        return sbU.toString();
    }

    public String PreparePlacesDetailsURL(String place_id){

        StringBuilder sbU = new StringBuilder();
        sbU.append(PlacesManager.DETAILS_ROOT_URL);
        sbU.append(PlacesManager.QSTRING_JSON);
        sbU.append(PlacesManager.PARAM_PLACEID + "=" + place_id);
        sbU.append("&" + PlacesManager.PARAM_LANGUAGE + "=" + AppSettings.LANGUAGE);
        sbU.append("&" + PlacesManager.PARAM_APIKEY + "=" + appContext.getString(R.string.google_directions_apikey));


        return sbU.toString();
    }


    public List<HashMap<String, String>> parsePlacesAutoComplete(GResponse response) {

        try {

            JSONObject data = (JSONObject) response.Data;

            JSONArray jPlaces = data.getJSONArray("predictions");

            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<HashMap<String, String>>();
            HashMap<String, String> place = null;

            for (int i = 0; i < placesCount; i++) {
                try {
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            return placesList;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * Parsing the Place JSON object
     */
    public HashMap<String, String> getPlace(JSONObject jPlace) {

        HashMap<String, String> place = new HashMap<String, String>();

        String place_id = "";
        String reference = "";
        String description = "";

        try {

            description = jPlace.getString("description");
            place_id = jPlace.getString("place_id");
            reference = jPlace.getString("reference");

            place.put("description", description);
            place.put("place_id", place_id);
            place.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return place;
    }


    private Response.Listener<JSONObject> createReqSuccessListener(final GResponse.RequestTypes _type) {
        return new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d(TAG, "response : " + response);

                GResponse gResp = new GResponse(_type, response, GResponse.ResponseStatus.Success);
                CommManager.getInstance().TriggerResponse(gResp);

            }
        };
    }

    private Response.ErrorListener createReqErrorListener(final GResponse.RequestTypes _type) {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "error : " + error.getMessage());

                GResponse gResp = new GResponse(_type, error, GResponse.ResponseStatus.Error);
                CommManager.getInstance().TriggerResponse(gResp);

            }
        };
    }


}
