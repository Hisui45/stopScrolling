package com.example.stopscrolling.ui.home;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import com.example.stopscrolling.MainActivity;

import java.util.Set;

public class AppData {

    private Drawable ICON;
    private String NAME;

    private String packageName;
    private boolean isActive = false;


    public AppData(Drawable icon, String name, String packageName) {
        ICON = icon;
        NAME = name;
        this.packageName = packageName;
        isActive = false;
    }

    public Drawable getICON() {
        return ICON;
    }

    public String getNAME() {
        return NAME;
    }

    public boolean isActive(MainActivity mainActivity) {
        SharedPreferences sharedPreferences = mainActivity.getPreferences(Context.MODE_PRIVATE);
        Set<String> set = sharedPreferences.getStringSet("activated_apps", null);
        if(set != null){
            Object[] activatedApps = set.toArray();
            for(Object app : activatedApps){
                if(app.toString().equalsIgnoreCase(packageName)){
                    return true;
                }
            }
        }
        return false;
    }

    public void setActive(boolean active) {

        isActive = active;
    }

    public String getPackageName() {
        return packageName;
    }
}
