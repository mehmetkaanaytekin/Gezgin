package com.mirketech.gezgin;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.mirketech.gezgin.util.AppSettings;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RouteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteFragment extends Fragment {

    private static final String TAG = RouteFragment.class.getSimpleName();

    private GoogleMap googleMap;
    private Marker mMarker;
    private MapView mMapView;
    private FloatingActionButton mFabAction;

    private OnFragmentInteractionListener mListener;

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
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View v = inflater.inflate(R.layout.fragment_route, container,
                false);

        mFabAction = (FloatingActionButton)v.findViewById(R.id.fabAction);
        mFabAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"FabAction clicked.");
            }
        });

        initMap(v, savedInstanceState);

        return v;
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

            Log.e(TAG, "onMyLocationChange");

            LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

            if (mMarker != null) {
                mMarker.remove();
            }
            if (googleMap != null) {
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

//        googleMap.addPolyline((new PolylineOptions())
//                .add(istanbul, sakarya).width(0.8F));

//        googleMap.setOnPolylineClickListener(new GoogleMap.OnPolylineClickListener() {
//            @Override
//            public void onPolylineClick(Polyline polyline) {
//                int strokeColor = polyline.getColor() ^ 0x00ffffff;
//                polyline.setColor(strokeColor);
//            }
//        });
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
    }

}
