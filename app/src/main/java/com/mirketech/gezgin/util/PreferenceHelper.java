package com.mirketech.gezgin.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by yasin.avci on 22.4.2016.
 */
public class PreferenceHelper {

    public static final String PREF_KEY							= "KEY"; //TODO add whatever preference you wanted to store !


    private Context appContext = null;


    public PreferenceHelper(Context _context) {
        appContext = _context;

    }

    public String GetString(String _key, String _default) {
        return PreferenceManager.getDefaultSharedPreferences(appContext).getString(_key, _default);
    }

    public Boolean GetBoolean(String _key, Boolean _default) {
        return PreferenceManager.getDefaultSharedPreferences(appContext).getBoolean(_key, _default);
    }

    public Integer GetInteger(String _key, Integer _default) {
        return PreferenceManager.getDefaultSharedPreferences(appContext).getInt(_key, _default);
    }

    public Boolean ClearPreferences() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.clear();
        return editor.commit();
    }

    public Boolean SetString(String _key, String _value) {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putString(_key, _value);
        return editor.commit();

    }

    public Boolean SetBoolean(String _key, Boolean _value) {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putBoolean(_key, _value);
        return editor.commit();

    }

    public Boolean SetInteger(String _key, Integer _value) {

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(appContext).edit();
        editor.putInt(_key, _value);
        return editor.commit();

    }

}
