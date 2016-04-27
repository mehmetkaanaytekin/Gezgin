package com.mirketech.gezgin.comm;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by yasin.avci on 22.4.2016.
 */
public class CommManager {
    private static final String TAG = CommManager.class.getSimpleName();

    private static CommManager ourInstance = null;

    private ArrayList<ICommResponse> ResponseListeners;

    public static synchronized CommManager getInstance() {
        if (ourInstance != null) {
            return ourInstance;
        } else {
            ourInstance = new CommManager();
        }

        return ourInstance;

    }

    private CommManager() {
        ResponseListeners = new ArrayList<>();
    }

    public void SetResponseListener(ICommResponse listener) {

        if (ResponseListeners == null) {
            ResponseListeners = new ArrayList<>();
        }

        ResponseListeners.add(listener);

    }

    public void TriggerResponse(GResponse response) {

        Log.d(TAG,".TriggerResponse: RequestType / Status / Data = " + response.RequestType.toString() + " / " + response.Status.toString() + " / " + response.Data);

        if (ResponseListeners != null) {
            for (ICommResponse listener : ResponseListeners) {
                listener.onResponse(response);
            }

        }


    }


}
