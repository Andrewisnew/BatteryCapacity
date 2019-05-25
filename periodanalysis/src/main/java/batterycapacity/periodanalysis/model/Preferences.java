package batterycapacity.periodanalysis.model;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private static final String APP_PREFERENCES = "app_preferences";
    private static final String LEVEL = "level";
    private SharedPreferences sPref;

    public Preferences(Context context) {
        sPref = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    public void saveLevel(int capacity) {
        SharedPreferences.Editor ed = sPref.edit();
        ed.putInt(LEVEL, capacity);
        ed.apply();
    }

    public int loadLevel() {
        return sPref.getInt(LEVEL, 100);
    }
}
