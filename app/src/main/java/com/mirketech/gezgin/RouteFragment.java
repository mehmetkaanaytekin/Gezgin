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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.dmitrymalkovich.android.ProgressFloatingActionButton;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mirketech.gezgin.comm.CommManager;
import com.mirketech.gezgin.comm.GResponse;
import com.mirketech.gezgin.comm.ICommResponse;
import com.mirketech.gezgin.direction.DirectionManager;
import com.mirketech.gezgin.models.SuggestModel;
import com.mirketech.gezgin.places.PlacesManager;
import com.mirketech.gezgin.util.AppSettings;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RouteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteFragment extends Fragment implements ICommResponse {

    private static final String TAG = RouteFragment.class.getSimpleName();

    private ListView lstSearchSuggestions;

    private ProgressFloatingActionButton progFabLoading;
    private GoogleMap googleMap;
    private MapView mMapView;
    private FloatingActionButton mFabAction;
    private FloatingActionButton mFabClear;
    private LatLng latestMyLocation;
    private OnFragmentInteractionListener mListener;
    private boolean isInterrupted = false;
    private List<Marker> lstMarkers;
    private List<SuggestModel> lstSuggestionsData = new ArrayList<>();


    public RouteFragment() {
        // Required empty public constructor


    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment RouteFragment.
     */
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

    private void clearMap() {
        if (googleMap != null) {
            googleMap.clear();
            lstMarkers.clear();
            mFabClear.setVisibility(View.INVISIBLE);
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

            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, AppSettings.CAMERA_DEFAULT_DIRECTION_PADDING_INPX);
            googleMap.animateCamera(update, AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);


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

            CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, AppSettings.CAMERA_DEFAULT_DIRECTION_PADDING_INPX);
            googleMap.animateCamera(update, AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);


        } catch (Exception e) {
            e.printStackTrace();

        }


    }

    private void parsePlacesAutoCompleteResponse(GResponse response) {


        List<HashMap<String, String>> placesList = PlacesManager.getInstance(getActivity()).parsePlacesAutoComplete(response);

        Log.d(TAG, "placesList.size : " + placesList.size());

        progFabLoading.setVisibility(View.GONE);

        for (HashMap<String, String> item : placesList) {

//            Log.d(TAG, "item desc : " + item.get("description"));
//            Log.d(TAG, "item _id : " + item.get("_id"));
//            Log.d(TAG, "item reference : " + item.get("reference"));


            PlacesManager.getInstance(getActivity()).GetPlaceDetails(item.get("place_id"));


        }


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

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {

            //Log.d(TAG, "onMyLocationChange");

            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            latestMyLocation = loc;

            if (googleMap != null && !isInterrupted) {
                CameraUpdate update = CameraUpdateFactory.newLatLngZoom(loc, AppSettings.CAMERA_DEFAULT_MY_LOCATION_ZOOM_LEVEL);
                googleMap.animateCamera(update, AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);
            }
        }
    };

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


        googleMap.moveCamera(CameraUpdateFactory.newLatLng(AppSettings.MAP_DEFAULT_LOCATION));
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(AppSettings.CAMERA_DEFAULT_ZOOM_LEVEL), AppSettings.CAMERA_DEFAULT_ANIMATE_DURATION_MS, null);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                isInterrupted = false;
                return false;
            }
        });

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


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onStart() {
        super.onStart();
        lstMarkers = new ArrayList<>();
        CommManager.getInstance().SetResponseListener(this);
    }

}
