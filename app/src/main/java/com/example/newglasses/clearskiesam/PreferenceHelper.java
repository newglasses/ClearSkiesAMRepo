package com.example.newglasses.clearskiesam;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by newglasses on 09/08/2016.
 */
public class PreferenceHelper {

    //SharedPreferences sharedPrefs;

    public static boolean getUpdateOnlyOnWifi(Context context){

        //PreferenceManager.getDefaultSharedPreferences(context);
        return false;


    }

    public static boolean getUpdateCheckDaily(Context context){

        //PreferenceManager.getDefaultSharedPreferences(context);
        return true;


    }
}
