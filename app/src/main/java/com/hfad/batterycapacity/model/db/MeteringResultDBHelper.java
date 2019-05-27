package com.hfad.batterycapacity.model.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hfad.batterycapacity.entities.MeteringResult;

import java.util.ArrayList;
import java.util.List;

public class MeteringResultDBHelper extends BatteryCapacityDBHelper {

    private static final String TABLE_NAME = "METERING_RESULT";

    public void deleteAll() {
        getWritableDatabase().execSQL("delete from " + TABLE_NAME);
    }

    enum Columns {
        SUM_OF_POWERS,
        AVERAGE_VOLTAGE,
        START_LEVEL,
        FINISH_LEVEL
    }

    public MeteringResultDBHelper(Context context) {
        super(context);
    }

    static void createTable(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_NAME
                + " (_id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Columns.SUM_OF_POWERS + " REAL, "
                + Columns.AVERAGE_VOLTAGE + " REAL, "
                + Columns.START_LEVEL + " INTEGER, "
                + Columns.FINISH_LEVEL + " INTEGER);");
    }

    public void insert(MeteringResult meteringResult) {
        ContentValues stateValues = new ContentValues();
        stateValues.put(Columns.SUM_OF_POWERS.name(), meteringResult.getSumOfPowers());
        stateValues.put(Columns.AVERAGE_VOLTAGE.name(), meteringResult.getAvgVoltage());
        stateValues.put(Columns.START_LEVEL.name(), meteringResult.getStartLevel());
        stateValues.put(Columns.FINISH_LEVEL.name(), meteringResult.getFinishLevel());
        getWritableDatabase().insert(TABLE_NAME, null, stateValues);
    }

    public List<MeteringResult> getAll() {
        Cursor cursor = getReadableDatabase().query(TABLE_NAME,
                new String[]{Columns.SUM_OF_POWERS.name(),
                             Columns.AVERAGE_VOLTAGE.name(),
                             Columns.START_LEVEL.name(),
                             Columns.FINISH_LEVEL.name()},
                null, null, null, null,
                null, null);
        ArrayList<MeteringResult> meteringResults = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                meteringResults.add(new MeteringResult(
                        cursor.getDouble(0),
                        cursor.getDouble(1),
                        cursor.getInt(2),
                        cursor.getInt(3)));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return meteringResults;
    }
}
