package com.hfad.batterycapacity.config;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String APP_PREFERENCES = "app_preferences";
    private static final String PERIOD = "period";
    private SharedPreferences sPref;

    public Preferences(Context context) {
        sPref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void savePeriod(int period) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(PERIOD, period);
        ed.apply();
    }

    public int loadPeriod() {
        return sPref.getInt(PERIOD, -1);
    }
}
