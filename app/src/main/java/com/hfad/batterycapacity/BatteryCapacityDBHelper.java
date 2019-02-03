package com.hfad.batterycapacity;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BatteryCapacityDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "battery_capacity";
    private static final int DB_VERSION = 1;
    BatteryCapacityDBHelper(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db){
        updateDatabase(db, 0, DB_VERSION);
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        updateDatabase(db, oldVersion, newVersion);
    }

    private void updateDatabase(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 1) {
            db.execSQL("CREATE TABLE IF NOT EXISTS STATE (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "VOLTAGE REAL, "
                    + "CURRENT REAL, "
                    + "LEVEL INTEGER);");
            db.execSQL("CREATE TABLE IF NOT EXISTS VALS (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + "NAME TEXT, "
                    + "VAL INTEGER);");
        }
    }
    public static void insert(SQLiteDatabase db, double voltage, double current, int level) {
        ContentValues stateValues = new ContentValues();
        stateValues.put("VOLTAGE", voltage);
        stateValues.put("CURRENT", current);
        stateValues.put("LEVEL", level);
        db.insert("STATE", null, stateValues);
    }
}
