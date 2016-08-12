package com.mirketech.gezgin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mirketech.gezgin.R;
import com.mirketech.gezgin.models.PlaceModel;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by yasin.avci on 23.5.2016.
 */
public class PlacesAdapter extends ArrayAdapter<PlaceModel> {

    // View lookup cache
    private static class ViewHolder {
        TextView title;
        TextView desc;
        ImageView markerimg;

    }

    public PlacesAdapter(Context context, List<PlaceModel> places) {
        super(context, R.layout.row_place_list, places);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        PlaceModel place = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_place_list, parent, false);
            viewHolder.title = (TextView) convertView.findViewById(R.id.txtPlaceTitle);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.txtPlaceDesc);
            viewHolder.markerimg = (ImageView) convertView.findViewById(R.id.imgPlaceMarker);
            convertView.setTag(viewHolder);


        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.title.setText(place.GetTitle());
        viewHolder.desc.setText(place.GetDesc());
        viewHolder.markerimg.setImageBitmap(place.GetBitmapMarker());


        return convertView;
    }
}
