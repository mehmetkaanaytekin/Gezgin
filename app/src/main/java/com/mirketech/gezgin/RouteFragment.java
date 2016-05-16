package com.mirketech.gezgin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bowyer.app.fabtoolbar.FabToolbar;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.mirketech.gezgin.adapters.CustomInfoWinAdapter;
import com.mirketech.gezgin.adapters.SuggestionAdapter;
import com.mirketech.gezgin.comm.CommManager;
import com.mirketech.gezgin.comm.GResponse;
import com.mirketech.gezgin.comm.ICommResponse;
import com.mirketech.gezgin.direction.DirectionManager;
import com.mirketech.gezgin.listeners.ViewAnimatorListener;
import com.mirketech.gezgin.models.SuggestModel;
import com.mirketech.gezgin.places.PlacesManager;
import com.mirketech.gezgin.util.AppSettings;
import com.mirketech.gezgin.util.MapsHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;


public class RouteFragment extends Fragment implements ICommResponse {

    private static final String TAG = RouteFragment.class.getSimpleName();
    SuggestionAdapter searchSuggestAdapter = null;
    private static final int LOCATION_REQUEST_CODE = 861;

    //Views
    private ListView lstSearchSuggestions;
    private MapView mMapView;
    private FloatingActionButton mFabAction;
    private FloatingActionButton mFabClear;
    private LinearLayout linlayOrigin;
    private LinearLayout linlayDest;
    private FloatingActionButton mFabOrigin;
    private FloatingActionButton mFabDest;
    private FloatingActionButton mFabNearbyPlaces;
    private TextView txtOrigin;
    private TextView txtDest;
    private FabToolbar mFabToolbar;
    private ImageButton btnRouteSettings;
    private ImageButton btnCreateRoute;
    private SmoothProgressBar sProgBar;


    //Listeners
    private OnFragmentInteractionListener mListener;

    //Variables
    private GoogleMap googleMap;
    private LatLng latestMyLocation;
    private volatile boolean isInterrupted = false;
    private LatLng origin = null;
    private LatLng destination = null;
    private List<LatLng> lstDirections;


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
                animateInClearButton(true);
                animateInSearchSuggestions(true);
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

                animateOutSearchSuggestions(true);
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

        if (googleMap != null) {
            mMapView.onPause();
        }

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
        mFabToolbar = (FabToolbar) v.findViewById(R.id.fabtoolbar);
        btnRouteSettings = (ImageButton) v.findViewById(R.id.btnTbarRouteSettings);
        btnCreateRoute = (ImageButton) v.findViewById(R.id.btnTbarCreateRoute);
        mFabAction = (FloatingActionButton) v.findViewById(R.id.fabAction);
        mFabClear = (FloatingActionButton) v.findViewById(R.id.fabClear);
        mFabNearbyPlaces = (FloatingActionButton) v.findViewById(R.id.fabNearbyPlaces);
        linlayOrigin = (LinearLayout) v.findViewById(R.id.linlayOrigin);
        linlayDest = (LinearLayout) v.findViewById(R.id.linlayDest);
        mFabOrigin = (FloatingActionButton) v.findViewById(R.id.fabOrigin);
        mFabDest = (FloatingActionButton) v.findViewById(R.id.fabDest);
        txtOrigin = (TextView) v.findViewById(R.id.txtOrigin);
        txtDest = (TextView) v.findViewById(R.id.txtDest);
        sProgBar = (SmoothProgressBar) v.findViewById(R.id.sProgBar);

        txtOrigin.setText(getString(R.string.my_location));
        txtDest.setText("");


        mFabNearbyPlaces.setOnClickListener(fabNearbyPlacesClickListener);

        mFabAction.setOnClickListener(fabActionClickListener);

        mFabOrigin.setOnClickListener(fabOriginClickListener);

        mFabDest.setOnClickListener(fabDestClickListener);

        mFabClear.setOnClickListener(fabClearClickListener);

        btnCreateRoute.setOnClickListener(btnCreateRouteClickListener);

        btnRouteSettings.setOnClickListener(btnRouteSettingsClickListener);

        lstSearchSuggestions.setOnItemClickListener(SearchSuggestionsOnItemClickListener);

        lstSearchSuggestions.setOnTouchListener(SearchSuggestionsOnTouchListener);

        mFabToolbar.setFab(mFabAction);

        initMap(v, savedInstanceState);

        return v;
    }


    private View.OnTouchListener SearchSuggestionsOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            animateInSearchSuggestions(false);
            return false;
        }
    };

    private AdapterView.OnItemClickListener SearchSuggestionsOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            SuggestModel data = lstSuggestionsData.get(position);

            LatLng loc = new LatLng(data.getLatitude(), data.getLongitude());
            MapsHelper.moveCamera(googleMap, loc, AppSettings.CAMERA_DEFAULT_MY_LOCATION_ZOOM_LEVEL);

            for (Marker mrk : lstMarkers) {
                if (mrk.getPosition().latitude == data.getLatitude() && mrk.getPosition().longitude == data.getLongitude()) {
                    animateInDestination(true);
                    mrk.showInfoWindow();
                    break;
                }
            }

            animateOutSearchSuggestions(false);
        }
    };

    private GoogleMap.OnMarkerClickListener gMapMarkerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {

            isInterrupted = true;

            animateOutNearbyPlacesButton(true);
            animateOutSearchSuggestions(false);
            animateInClearButton(true);
            animateInDestination(true);

            return false;
        }
    };

    private View.OnClickListener btnRouteSettingsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                    .title(getString(R.string.route_settings))
                    .items(R.array.arr_route_settings)
                    .itemsCallbackMultiChoice(null, new MaterialDialog.ListCallbackMultiChoice() {
                        @Override
                        public boolean onSelection(MaterialDialog dialog, Integer[] which, CharSequence[] text) {

                            for (int i = 0; i < which.length; i++) {
                                switch (which[i]) {
                                    case 0:
                                        AppSettings.ROUTE_AVOID_TOLLS = true;
                                        break;
                                    case 1:
                                        AppSettings.ROUTE_AVOID_HIGHWAYS = true;
                                        break;
                                    case 2:
                                        AppSettings.ROUTE_AVOID_FERRIES = true;
                                        break;

                                }
                            }

                            return true;
                        }
                    })
                    .positiveText(getString(R.string.route_settings_dialog_save))
                    .theme(Theme.DARK)
                    .show();

            ArrayList<Integer> lstIndices = new ArrayList<Integer>();

            if (AppSettings.ROUTE_AVOID_TOLLS) {
                lstIndices.add(0);
            }
            if (AppSettings.ROUTE_AVOID_HIGHWAYS) {
                lstIndices.add(1);
            }
            if (AppSettings.ROUTE_AVOID_FERRIES) {
                lstIndices.add(2);
            }


            if (lstIndices.size() > 0) {

                Integer selectedIndices[] = new Integer[lstIndices.size()];

                for (int i = 0; i < lstIndices.size(); i++) {
                    selectedIndices[i] = lstIndices.get(i);
                }

                dialog.setSelectedIndices(selectedIndices);
            }
        }
    };

    private View.OnClickListener fabNearbyPlacesClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            // TODO implement nearby places !

            PlacesManager.getInstance(getActivity()).GetPlacesNearby(lstDirections, "");


        }
    };


    private View.OnClickListener fabClearClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            clearMap();
        }
    };

    private View.OnClickListener btnCreateRouteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mFabToolbar.slideOutFab();
            if (latestMyLocation != null && destination != null) {
                if (origin == null) {
                    origin = latestMyLocation;
                }
                lstDirections = null;

                sProgBar.setVisibility(View.VISIBLE);

                DirectionManager.getInstance(getActivity()).GetDirections(origin, destination);

                animateOutActionButton(true);
            }
        }
    };

    private View.OnClickListener fabActionClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Log.d(TAG, "FabAction clicked.");

            mFabToolbar.expandFab();
            mFabToolbar.bringToFront();
        }
    };
    private View.OnClickListener fabOriginClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (Marker mrk : lstMarkers) {
                if (mrk.isInfoWindowShown()) {
                    txtOrigin.setText(mrk.getTitle());
                    origin = mrk.getPosition();
                    if (destination != null) {
                        animateInActionButton(true);
                    }
                }
            }
        }
    };

    private View.OnClickListener fabDestClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            for (Marker mrk : lstMarkers) {
                if (mrk.isInfoWindowShown()) {
                    txtDest.setText(mrk.getTitle());
                    destination = mrk.getPosition();
                    animateInActionButton(true);
                    break;
                }
            }
        }
    };

    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {

        @Override
        public void onMyLocationChange(Location location) {

            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            latestMyLocation = loc;

            animateInOrigin(true);

            if (googleMap != null && !isInterrupted) {
                MapsHelper.moveCamera(googleMap, loc, AppSettings.CAMERA_DEFAULT_MY_LOCATION_ZOOM_LEVEL);
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                    googleMap.setMyLocationEnabled(true);
                }
            }
        }


    }


    private void clearMap() {
        if (googleMap != null) {
            lstSuggestionsData.clear();
            googleMap.clear();
            lstMarkers.clear();
            if (searchSuggestAdapter != null) {
                searchSuggestAdapter.notifyDataSetChanged();
            }

            animateOutSearchSuggestions(true);
            animateOutClearButton(true);
            animateOutDestination(true);
            origin = null;
            destination = null;
            txtDest.setText("");
            txtOrigin.setText(getString(R.string.my_location));

            if (mFabToolbar.isFabExpanded()) {
                mFabToolbar.slideOutFab();
            }

            animateOutNearbyPlacesButton(true);
            animateOutActionButton(true);
        }
    }

    private void EnableMyLocation() {

        Log.d(TAG, "EnableMyLocation");

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);

            return;
        }

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

        googleMap.getUiSettings().setMapToolbarEnabled(false);
        googleMap.getUiSettings().setCompassEnabled(false);


        googleMap.setOnMyLocationChangeListener(myLocationChangeListener);
        googleMap.setOnMarkerClickListener(gMapMarkerClickListener);
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (mFabToolbar.isFabExpanded()) {
                    mFabToolbar.slideOutFab();
                }
            }
        });
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {

                String title = Location.convert(latLng.latitude, Location.FORMAT_DEGREES) + " , " + Location.convert(latLng.longitude, Location.FORMAT_DEGREES);
                Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(title));
                lstMarkers.add(marker);

                animateInClearButton(true);
            }
        });

        googleMap.setInfoWindowAdapter(new CustomInfoWinAdapter(getActivity()));

        EnableMyLocation();


        MapsHelper.moveCamera(googleMap, AppSettings.MAP_DEFAULT_LOCATION, AppSettings.CAMERA_DEFAULT_ZOOM_LEVEL);


        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                isInterrupted = false;
                origin = null;
                txtOrigin.setText(getString(R.string.my_location));
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
        if (response.RequestType.equals(GResponse.RequestTypes.Places_NearbySearch)) {

            parsePlacesNearbySearchResponse(response);

        }


    }

    private void parsePlacesNearbySearchResponse(GResponse response) {
        try {
            JSONObject data = (JSONObject) response.Data;

            JSONArray resultsArr = data.getJSONArray("results");


            for (int i = 0; i < resultsArr.length(); i++) {

                JSONObject place = resultsArr.getJSONObject(i);

                JSONObject place_loc = place.getJSONObject("geometry").getJSONObject("location");

                double lat = place_loc.getDouble("lat");
                double lng = place_loc.getDouble("lng");

                LatLng pos = new LatLng(lat, lng);

                String place_name = place.getString("name");

                MarkerOptions mOpt = new MarkerOptions();
                mOpt.position(pos);


                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(pos)
                        .alpha(0.5F)
                        .title(place_name));


                lstMarkers.add(marker);


            }


        } catch (Exception e) {

        }


    }

    private void parseDirectionResponse(GResponse response) {
        try {
            isInterrupted = true;


            lstDirections = DirectionManager.getInstance(getActivity()).ParseDirectionResponse(response);


            PolylineOptions polylineOptions = new PolylineOptions();
            polylineOptions.addAll(lstDirections);
            polylineOptions
                    .width(AppSettings.ROUTE_WIDTH)
                    .color(AppSettings.ROUTE_COLOR);

            googleMap.addPolyline(polylineOptions);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            for (LatLng pos : lstDirections) {
                builder.include(pos);
            }
            LatLngBounds bounds = builder.build();

            MapsHelper.moveCamera(googleMap, bounds);


            animateInClearButton(true);

            hideMarkerInfoWindows();


            animateInNearbyPlacesButton(true);

            sProgBar.setVisibility(View.GONE);


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


        for (HashMap<String, String> item : placesList) {

            PlacesManager.getInstance(getActivity()).GetPlaceDetails(item.get("place_id"));
        }


    }


    private void hideMarkerInfoWindows() {
        for (Marker mrk : lstMarkers) {
            if (mrk.isInfoWindowShown()) {
                mrk.hideInfoWindow();
            }
        }

    }


    private void animateOutSearchSuggestions(final boolean hide) {
        lstSearchSuggestions.animate().translationY(50 - lstSearchSuggestions.getHeight()).setListener(new ViewAnimatorListener(hide, false, lstSearchSuggestions));
    }

    private void animateInSearchSuggestions(final boolean show) {
        lstSearchSuggestions.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, lstSearchSuggestions));
    }

    private void animateOutClearButton(final boolean hide) {
        mFabClear.animate().translationY(0 - mFabClear.getHeight()).setListener(new ViewAnimatorListener(hide, false, mFabClear));
    }

    private void animateInClearButton(final boolean show) {
        if (!show) {
            mFabClear.setY(0 - mFabClear.getHeight());
            mFabClear.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabClear));
        } else {
            if (mFabClear.getVisibility() == View.GONE) {
                mFabClear.setY(0 - mFabClear.getHeight());
                mFabClear.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabClear));
            }
        }

    }

    private void animateOutActionButton(final boolean hide) {
        mFabAction.animate().translationY(50 + mFabAction.getHeight()).setListener(new ViewAnimatorListener(hide, false, mFabAction));
    }

    private void animateInActionButton(final boolean show) {

        if (!show) {

            mFabAction.setY(mMapView.getHeight() + 50 + mFabAction.getHeight());
            mFabAction.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabAction));
        } else {
            if (mFabAction.getVisibility() == View.GONE) {
                mFabAction.setY(mMapView.getHeight() + 50 + mFabAction.getHeight());
                mFabAction.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabAction));
            }
        }

    }

    private void animateInOrigin(final boolean show) {
        if (!show) {

            linlayOrigin.setX(0 - linlayOrigin.getWidth());
            linlayOrigin.animate().translationX(0).setListener(new ViewAnimatorListener(false, show, linlayOrigin));
        } else {
            if (linlayOrigin.getVisibility() == View.GONE) {
                linlayOrigin.setX(0 - linlayOrigin.getWidth());
                linlayOrigin.animate().translationX(0).setListener(new ViewAnimatorListener(false, show, linlayOrigin));
            }
        }
    }

    private void animateOutOrigin(final boolean hide) {
        linlayOrigin.animate().translationX(50 - linlayOrigin.getWidth()).setListener(new ViewAnimatorListener(hide, false, linlayOrigin));
    }

    private void animateInDestination(final boolean show) {
        if (!show) {

            linlayDest.setX(50 + mMapView.getWidth() + linlayDest.getWidth());
            linlayDest.animate().translationX(0).setListener(new ViewAnimatorListener(false, show, linlayDest));
        } else {
            if (linlayDest.getVisibility() == View.GONE) {
                linlayDest.setX(50 + mMapView.getWidth() + linlayDest.getWidth());
                linlayDest.animate().translationX(0).setListener(new ViewAnimatorListener(false, show, linlayDest));
            }
        }
    }

    private void animateOutDestination(final boolean hide) {
        linlayDest.animate().translationX(50 + mMapView.getWidth() + linlayDest.getWidth()).setListener(new ViewAnimatorListener(hide, false, linlayDest));
    }

    private void animateOutNearbyPlacesButton(final boolean hide) {
        mFabNearbyPlaces.animate().translationY(50 + mFabNearbyPlaces.getHeight()).setListener(new ViewAnimatorListener(hide, false, mFabNearbyPlaces));
    }

    private void animateInNearbyPlacesButton(final boolean show) {

        if (!show) {

            mFabNearbyPlaces.setY(mMapView.getHeight() + 50 + mFabNearbyPlaces.getHeight());
            mFabNearbyPlaces.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabNearbyPlaces));
        } else {
            if (mFabNearbyPlaces.getVisibility() == View.GONE) {
                mFabNearbyPlaces.setY(mMapView.getHeight() + 50 + mFabNearbyPlaces.getHeight());
                mFabNearbyPlaces.animate().translationY(0).setListener(new ViewAnimatorListener(false, show, mFabNearbyPlaces));
            }
        }

    }


}
