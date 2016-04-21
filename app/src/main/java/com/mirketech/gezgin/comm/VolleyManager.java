package com.mirketech.gezgin.comm;

import android.app.ActivityManager;
import android.content.Context;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

/**
 * Created by yasin.avci on 21.4.2016.
 */
public class VolleyManager {
    private static VolleyManager ourInstance = null;
    private Context appContext;

    private RequestQueue mRequestQueue;
    //private ImageLoader mImageLoader;


    public static VolleyManager getInstance(Context _context) {
        if(ourInstance != null){
            return ourInstance;
        }else{
            ourInstance = new VolleyManager(_context);
        }

        return ourInstance;
    }

    private VolleyManager(Context context) {
        appContext = context;

        mRequestQueue = Volley.newRequestQueue(context);

    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue != null) {
            return mRequestQueue;
        } else {
            throw new IllegalStateException("RequestQueue not initialized");
        }
    }





}
