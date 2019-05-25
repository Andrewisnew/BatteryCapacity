package batterycapacity.periodanalysis.model.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class BatteryCapacityDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "battery_capacity";
    private static final int DB_VERSION = 1;
    protected static final String LOG = "LOG";
    protected BatteryCapacityDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        Log.i(LOG, getClass().toString() + " onCreate");
        updateDatabase(db, 0, DB_VERSION);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < DB_VERSION) {
            BatteryStateDBHelper.createTable(db);
        }
    }
}
