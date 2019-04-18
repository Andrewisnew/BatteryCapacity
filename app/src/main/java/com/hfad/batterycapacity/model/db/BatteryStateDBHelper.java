package com.hfad.batterycapacity.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hfad.batterycapacity.entities.BatteryState;

import java.util.ArrayList;
import java.util.List;

public class BatteryStateDBHelper extends BatteryCapacityDBHelper {

    private static final String TABLE_NAME = "BATTERY_STATE";

    public void deleteAll() {
        getWritableDatabase().execSQL("delete from " + TABLE_NAME);
    }

    enum Columns {
        VOLTAGE,
        CURRENT,
        LEVEL
    }

    public BatteryStateDBHelper(Context context) {
        super(context);
    }

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Columns.VOLTAGE + " REAL, "
                + Columns.CURRENT + " REAL, "
                + Columns.LEVEL + " INTEGER);");
    }

    public void insert(double voltage, double current, int level) {
        ContentValues stateValues = new ContentValues();
        stateValues.put(Columns.VOLTAGE.name(), voltage);
        stateValues.put(Columns.CURRENT.name(), current);
        stateValues.put(Columns.LEVEL.name(), level);
        getWritableDatabase().insert(TABLE_NAME, null, stateValues);
    }

    public List<BatteryState> getLast(int limit) {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME,
                new String[]{Columns.VOLTAGE.name(), Columns.CURRENT.name(), Columns.LEVEL.name()},
                null, null, null, null,
                "_id DESC", String.valueOf(limit));
        ArrayList<BatteryState> states = new ArrayList<>();
        if (cursor.moveToLast()) {
            do {
                states.add(new BatteryState(cursor.getDouble(0),
                        cursor.getDouble(1),
                        cursor.getInt(2)));
            }while (cursor.moveToPrevious());
        }
        cursor.close();
        return states;
    }

    public List<BatteryState> getAll() {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME,
                new String[]{Columns.VOLTAGE.name(), Columns.CURRENT.name(), Columns.LEVEL.name()},
                null, null, null, null,
                null, null);
        ArrayList<BatteryState> states = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                states.add(new BatteryState(cursor.getDouble(0),
                        cursor.getDouble(1),
                        cursor.getInt(2)));
            }while (cursor.moveToPrevious());
        }
        cursor.close();
        return states;
    }

    public double getAvgVoltage() {
        Cursor cursor = null;
        try {
            cursor = getReadableDatabase()
                    .rawQuery("SELECT AVG(" + Columns.VOLTAGE + ") FROM " + TABLE_NAME,
                            null);
            cursor.moveToFirst();
            return cursor.getDouble(0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
