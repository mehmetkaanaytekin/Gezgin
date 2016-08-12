package com.mirketech.gezgin.listeners;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by yasin.avci on 23.5.2016.
 */
public class MultiCamChangeListener implements GoogleMap.OnCameraChangeListener {


    private List<GoogleMap.OnCameraChangeListener> mListeners = new ArrayList<GoogleMap.OnCameraChangeListener>();

    public void registerListener(GoogleMap.OnCameraChangeListener listener) {
        mListeners.add(listener);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

        for (GoogleMap.OnCameraChangeListener ccl: mListeners)
        {
            ccl.onCameraChange(cameraPosition);
        }
    }
}
