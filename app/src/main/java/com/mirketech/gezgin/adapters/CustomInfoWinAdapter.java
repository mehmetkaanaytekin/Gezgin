package com.mirketech.gezgin.adapters;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.mirketech.gezgin.R;

/**
 * Created by yasin.avci on 5.5.2016.
 */
public class CustomInfoWinAdapter implements GoogleMap.InfoWindowAdapter {

    private static final String TAG = CustomInfoWinAdapter.class.getSimpleName();
    private final View myContentsView;
    private Activity activity;

    private ImageButton btnCustomOrigin;
    //private ImageButton btnCustomDest;

    public CustomInfoWinAdapter(Activity act){

        activity = act;
        myContentsView = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {

        TextView txtCustomInfo = (TextView)myContentsView.findViewById(R.id.txtCustomInfo);
        btnCustomOrigin = (ImageButton)myContentsView.findViewById(R.id.btnCustomOrigin);
        //btnCustomDest = (ImageButton)myContentsView.findViewById(R.id.btnCustomDest);
        txtCustomInfo.setText(marker.getTitle());

        btnCustomOrigin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"btnCustomOrigin clicked.");
            }
        });
//        btnCustomDest.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG,"btnCustomDest clicked.");
//            }
//        });


        return myContentsView;
    }
}
