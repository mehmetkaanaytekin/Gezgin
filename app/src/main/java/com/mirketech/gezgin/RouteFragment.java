package com.mirketech.gezgin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dmitrymalkovich.android.ProgressFloatingActionButton;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mirketech.gezgin.adapters.SuggestionAdapter;
import com.mirketech.gezgin.comm.CommManager;
import com.mirketech.gezgin.comm.GResponse;
import com.mirketech.gezgin.comm.ICommResponse;
import com.mirketech.gezgin.direction.DirectionManager;
import com.mirketech.gezgin.models.SuggestModel;
import com.mirketech.gezgin.places.PlacesManager;
import com.mirketech.gezgin.util.AppSettings;
import com.mirketech.gezgin.util.MapsHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 *
 */
public class RouteFragment extends Fragment implements ICommResponse {

    private static final String TAG = RouteFragment.class.getSimpleName();
    SuggestionAdapter searchSuggestAdapter = null;
    //Views
    private ListView lstSearchSuggestions;
    private ProgressFloatingActionButton progFabLoading;
    private MapView mMapView;
    private FloatingActionButton mFabAction;
    private FloatingActionButton mFabClear;
    //Listeners
    private OnFragmentInteractionListener mListener;
    //Variables
    private GoogleMap googleMap;
    private LatLng latestMyLocation;
    private volatile boolean isInterrupted = false;
    //Data
    private List<Marker> lstMarkers;
    private ArrayList<SuggestModel> lstSuggestionsData = new ArrayList<>();


    public RouteFragment() {
        // Required empty public constructor


    }

    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        Log.d(TAG, "onCreateOptionsMenu");
        getActivity().getMenuInflater().inflate(R.menu.route_menu, menu);


        MenuItem searchItem = menu.findItem(R.id.route_menu_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Log.d(TAG, "onQueryTextSubmit query : " + query);
                clearMap();

                progFabLoading.setVisibility(View.VISIBLE);
                mFabClear.setVisibility(View.VISIBLE);
                lstSearchSuggestions.setVisibility(View.VISIBLE);
                lstSearchSuggestions.bringToFront();
                PlacesManager.getInstance(getActivity()).GetPlacesAutoComplete(query.trim());


                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        // Define the listener
        MenuItemCompat.OnActionExpandListener expandListener = new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {

                lstSearchSuggestions.setVisibility(View.GONE);
                // Do something when action item collapses
                return true;  // Return true to collapse action view
            }

            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                // Do something when expanded
                return true;  // Return true to expand action view
            }
        };

        MenuItemCompat.setOnActionExpandListener(searchItem, expandListener);


        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStart() {
        super.onStart();
        lstMarkers = new ArrayList<>();
        CommManager.getInstance().SetResponseListener(this);
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_route, container,
                false);

        lstSearchSuggestions = (ListView) v.findViewById(R.id.lstSearchSuggestions);
        progFabLoading = (ProgressFloatingActionButton) v.findViewById(R.id.progFabLoading);

        mFabAction = (FloatingActionButton) v.findViewById(R.id.fabAction);
        mFabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "FabAction clicked.");

                if (latestMyLocation != null) {

                    DirectionManager.getInstance(getActivity()).GetDirections(latestMyLocation, AppSettings.MAP_DEFAULT_LOCATION);//for testing

                }

            }
        });

        lstSearchSuggestions.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                SuggestModel data = lstSuggestionsData.get(position);

                LatLng loc = new LatLng(data.getLatitude(), data.getLongitude());
                MapsHelper.moveCamera(googleMap, loc);

                for (Marker mrk : lstMarkers) {
                    if (mrk.getPosition().latitude == data.getLatitude() && mrk.getPosition().longitude == data.getLongitude()) {
                        mrk.showInfoWindow();
                        break;
                    }
                }

                lstSearchSuggestions.animate().translationY(50 - lstSearchSuggestions.getHeight());


            }
        });
        lstSearchSuggestions.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                lstSearchSuggestions.animate().translationY(0);

                return false;
            }
        });

        mFabClear = (FloatingActionButton) v.findViewById(R.id.fabClear);
        mFabClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearMap();
            }
        });

        initMap(v, savedInstanceState);


        return v;
    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {

            //Log.d(TAG, "onMyLocationChange");

            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            latestMyLocation = loc;

            if (googleMap != null && !isInterrupted) {
                MapsHelper.moveCamera(googleMap, loc);
            }
        }
    };

    private void clearMap() {
        if (googleMap != null) {
            lstSearchSuggestions.animate().translationY(0);
            lstSuggestionsData.clear();
            googleMap.clear();
            lstMarkers.clear();
            mFabClear.setVisibility(View.INVISIBLE);
        }
    }

    private void EnableMyLocation() {

        Log.d(TAG, "EnableMyLocation");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "EnableMyLocation permissions");
            return;
        }
        Log.d(TAG, "setMyLocationEnabled");

        googleMap.setMyLocationEnabled(true);
    }

    private void initMap(View v, Bundle savedInstanceState) {
        Log.d(TAG, "initMap");

        mMapView = (MapView) v.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();// needed to get the map to display immediately


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();

        //googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        googleMap.setOnMyLocationChangeListener(myLocationChangeListener);

        EnableMyLocation();


        MapsHelper.moveCamera(googleMap, AppSettings.MAP_DEFAULT_LOCATION);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                isInterrupted = false;
                return false;
            }
        });

    }

    @Override
    public void onResponse(GResponse response) {
        Log.d(TAG, "response received.");
        if (!response.Status.equals(GResponse.ResponseStatus.Success)) {
            Log.e(TAG, ".onResponse Status : " + response.Status);
            return;
        }


        if (response.RequestType.equals(GResponse.RequestTypes.Places_Autocomplete)) {

            parsePlacesAutoCompleteResponse(response);
        }

        if (response.RequestType.equals(GResponse.RequestTypes.Places_GetDetails)) {

            parsePlaceDetailsResponse(response);
        }

        if (response.RequestType.equals(GResponse.RequestTypes.Direction)) {

            parseDirectionResponse(response);
        }


    }

    private void parseDirectionResponse(GResponse response) {
        try {
            isInterrupted = true;


            List<LatLng> lstPolies = DirectionManager.getInstance(getActivity()).ParseDirectionResponse(response);

            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(lstPolies);
            polylineOptions
                    .width(7)
                    .color(Color.GREEN);

            googleMap.addPolyline(polylineOptions);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (LatLng pos : lstPolies) {
                builder.include(pos);
            }
            LatLngBounds bounds = builder.build();

            MapsHelper.moveCamera(googleMap, bounds);


            mFabClear.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parsePlaceDetailsResponse(GResponse response) {

        try {

            isInterrupted = true;

            JSONObject data = (JSONObject) response.Data;

            JSONObject result = data.getJSONObject("result").getJSONObject("geometry").getJSONObject("location");

            Double longitude = result.getDouble("lng");
            Double latitude = result.getDouble("lat");

            SuggestModel suggestion = new SuggestModel();

            suggestion.setLatitude(latitude);
            suggestion.setLongitude(longitude);
            suggestion.setName(data.getJSONObject("result").getString("name"));
            suggestion.setDescription(data.getJSONObject("result").getString("formatted_address"));

            lstSuggestionsData.add(suggestion);

            MarkerOptions mOpt = new MarkerOptions();
            mOpt.position(new LatLng(latitude, longitude));

            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .title(data.getJSONObject("result").getString("name")));


            lstMarkers.add(marker);


            ArrayList<LatLng> lstPositions = new ArrayList<>();

            for (Marker mrk : lstMarkers) {
                lstPositions.add(mrk.getPosition());
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (LatLng pos : lstPositions) {
                builder.include(pos);
            }
            LatLngBounds bounds = builder.build();

            MapsHelper.moveCamera(googleMap, bounds);


            if (searchSuggestAdapter == null) {
                searchSuggestAdapter = new SuggestionAdapter(getContext(), lstSuggestionsData);
            }

            lstSearchSuggestions.setAdapter(searchSuggestAdapter);

            searchSuggestAdapter.notifyDataSetChanged();


        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    private void parsePlacesAutoCompleteResponse(GResponse response) {


        List<HashMap<String, String>> placesList = PlacesManager.getInstance(getActivity()).parsePlacesAutoComplete(response);

        Log.d(TAG, "placesList.size : " + placesList.size());

        progFabLoading.setVisibility(View.GONE);

        for (HashMap<String, String> item : placesList) {

            PlacesManager.getInstance(getActivity()).GetPlaceDetails(item.get("place_id"));
        }


    }





}
