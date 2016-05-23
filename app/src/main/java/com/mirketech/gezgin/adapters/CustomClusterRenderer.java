package com.mirketech.gezgin.adapters;

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.mirketech.gezgin.models.PlaceModel;

/**
 * Created by yasin.avci on 23.5.2016.
 */
public class CustomClusterRenderer extends DefaultClusterRenderer<PlaceModel> {


    public CustomClusterRenderer(Context context, GoogleMap map, ClusterManager<PlaceModel> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(PlaceModel item, MarkerOptions markerOptions) {
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(item.GetBitmapMarker()));
        markerOptions.title(item.GetTitle());
        //super.onBeforeClusterItemRendered(item, markerOptions);
    }
}
