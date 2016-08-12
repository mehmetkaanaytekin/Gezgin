package com.mirketech.gezgin.listeners;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yasin.avci on 23.5.2016.
 */
public class MultiMarkerClickListener implements GoogleMap.OnMarkerClickListener {


    private List<GoogleMap.OnMarkerClickListener> mListeners = new ArrayList<GoogleMap.OnMarkerClickListener>();

    public void registerListener(GoogleMap.OnMarkerClickListener listener) {
        mListeners.add(listener);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        for (GoogleMap.OnMarkerClickListener ccl: mListeners)
        {
            ccl.onMarkerClick(marker);
        }

        return false;
    }
}
