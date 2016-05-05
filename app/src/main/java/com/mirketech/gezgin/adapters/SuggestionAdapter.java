package com.mirketech.gezgin.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.mirketech.gezgin.R;
import com.mirketech.gezgin.models.SuggestModel;

import java.util.ArrayList;

/**
 * Created by yasin.avci on 2.5.2016.
 */
public class SuggestionAdapter extends ArrayAdapter<SuggestModel> {

    // View lookup cache
    private static class ViewHolder {
        TextView name;
        TextView desc;
    }

    public SuggestionAdapter(Context context, ArrayList<SuggestModel> users) {
        super(context, R.layout.row_search_suggestion, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        SuggestModel suggestion = getItem(position);
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.row_search_suggestion, parent, false);
            viewHolder.name = (TextView) convertView.findViewById(R.id.txtSuggestName);
            viewHolder.desc = (TextView) convertView.findViewById(R.id.txtSuggestDesc);
            convertView.setTag(viewHolder);


        }else{
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.name.setText(suggestion.getName());
        viewHolder.desc.setText(suggestion.getDescription());


        return convertView;
    }
}
