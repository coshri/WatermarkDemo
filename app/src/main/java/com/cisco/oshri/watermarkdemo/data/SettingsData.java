package com.cisco.oshri.watermarkdemo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import java.util.HashMap;
import java.util.Map;

public class SettingsData {


    public static final String USER = "user";
    public static final String CATALOG_URL = "CATALOG URL";
    public static final String RECORDING_FOLDER ="recording folder";

    public static final String CONTROL_PLAN_URL = "control plane url";
    public static final String START_SESSIONS_URL = "START SESSIONS URL";
    public static final String CLOSE_SESSION_URL = "CLOSE SESSION URL";
    public static final String UPLOAD_URL = "upload url";
public static  final  String AUTO_PLAY_URL = "auto play url";


    static SharedPreferences sharedPreferences;



    public static  final String[] USERS_LIST = new String[]{"Blue","Yellow","Pink"};


    static Map<String,String> settings;

    static {
        settings = new HashMap<>();

        //set default value
        setValue(USER,null);
        setValue(CATALOG_URL,null);

        setValue(CONTROL_PLAN_URL,"http://192.168.1.100:8080/url/LIVE-NBA");
        setValue(START_SESSIONS_URL,"http://192.168.1.100:8081/sessions/open?id=");
        setValue(CLOSE_SESSION_URL,"http://192.168.1.100:8081/sessions/close?id=");
        setValue(RECORDING_FOLDER,"/storage/emulated/0/AzRecorderFree");
        setValue(UPLOAD_URL,"http://10.56.186.198:10080/file_loader/content?name=");
        setValue(AUTO_PLAY_URL,"http://192.168.1.100:8080/watch?uname=");
    }


    public static void setValue(String key,String value)
    {
        settings.put(key,value);

        if(sharedPreferences != null)
            sharedPreferences.edit().putString(key,value);
    }

    public static String getValue(String key)
    {
        if(sharedPreferences != null && sharedPreferences.contains(key))
            return sharedPreferences.getString(key,null);

        return settings.get(key);
    }


    public static void loadSharedPreferences(SharedPreferences prefs)
    {
        sharedPreferences = prefs;




    }

    public static ColorDrawable getUserColor()
    {
        String user = getValue(USER);
        if(user== null)
            return new ColorDrawable(Color.WHITE);

        switch (user.toUpperCase()) {
            case "YELLOW":
            return    new ColorDrawable(Color.YELLOW);

            case "BLUE":
                return new ColorDrawable(Color.BLUE);

            case "PINK":
                return new ColorDrawable(Color.RED);

            default:
              return new ColorDrawable(Color.TRANSPARENT);
        }
    }

}
