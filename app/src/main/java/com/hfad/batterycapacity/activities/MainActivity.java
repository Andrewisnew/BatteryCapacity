package com.hfad.batterycapacity.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hfad.batterycapacity.model.db.BatteryCapacityDBHelper;
import com.hfad.batterycapacity.services.MainIntentService;
import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.model.Preferences;


public class MainActivity extends Activity {

    public static final String ADD_STATE = "ADD_STATE";

    private static boolean created = false;

    private int historyRecordsSize;
    private SQLiteDatabase db;
    private int startLevel;
    private int curLevel;
    private static int NUM_OF_METERINGS_PER_HOUR;
    private Preferences preferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        created = true;
        try {
            db = new BatteryCapacityDBHelper(this).getReadableDatabase();
        } catch (SQLiteException e) {
            Log.d("SQLiteException", e.getMessage());
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        preferences = new Preferences(this);

        int p = preferences.loadPeriod();
        if(p == -1){
            Intent intent = new Intent(this, SetPeriodActivity.class);
            startActivity(intent);
        }else {
            NUM_OF_METERINGS_PER_HOUR = 3600 / p;

            addMeteringHistory();

            final TextView capacityValView = findViewById(R.id.capacity_value);

            BroadcastReceiver br = new BroadcastReceiver() {
                @SuppressLint("DefaultLocale")
                public void onReceive(Context context, Intent intent) {
                    Cursor cursor = db.query("STATE",
                            new String[]{"VOLTAGE", "CURRENT", "LEVEL"},
                            null, null, null, null, null);
                    if (cursor.moveToLast()) {
                        curLevel = cursor.getInt(2);
                        GridLayout gridLayout = findViewById(R.id.metering_history);
                        if (historyRecordsSize >= 30) {
                            gridLayout.removeAllViews();
                            cursor.moveToPosition(cursor.getPosition() - 30);
                            historyRecordsSize = 0;
                            for (int i = 0; i < 30; i++) {
                                addToHistory(cursor, gridLayout);
                                cursor.moveToNext();
                            }
                        } else {
                            addToHistory(cursor, gridLayout);
                        }

                        if (cursor.moveToFirst()) {
                            int capacity;
                            double sumOfPowers = 0;
                            startLevel = cursor.getInt(2);
                            do {
                                sumOfPowers += cursor.getDouble(0) * cursor.getDouble(1);
                            } while (cursor.moveToNext());

                            if (startLevel != curLevel) {
                                capacity = (int) (sumOfPowers / (getAvgVoltage() * NUM_OF_METERINGS_PER_HOUR)
                                        * 100 / (curLevel - startLevel) * 1000);
                                capacityValView.setText(String.valueOf(capacity));
                            } else {
                                capacityValView.setText("-");
                            }
                        }
                    }
                    cursor.close();
                }
            };
            registerReceiver(br, new IntentFilter(ADD_STATE));
            Intent intent = new Intent(this, MainIntentService.class);
            startService(intent);
        }
    }
    private double getAvgVoltage() {
        Cursor cursor=null;
        try {
            cursor = db.rawQuery("SELECT AVG(VOLTAGE) FROM STATE", null);
            cursor.moveToFirst();
            return cursor.getDouble(0);
        }finally {
            if(cursor!=null){
                cursor.close();
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void addToHistory(Cursor cursor, GridLayout gridLayout) {
        for (int i = 0; i < 3; i++) {
            TextView textView = new TextView(this);
            textView.setPadding(5, 0, 5, 0);
            textView.setGravity(Gravity.CENTER);
            textView.setText(i < 2 ? String.format("%.2f",
                    cursor.getDouble(i)) : String.valueOf(cursor.getInt(i)));
            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                    GridLayout.spec(i),
                    GridLayout.spec(historyRecordsSize)
            );
            gridLayout.addView(textView, layoutParams);
        }
        historyRecordsSize++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        created = false;
        if (db != null) {
            db.close();
        }
    }

    @SuppressLint("DefaultLocale")
    private void addMeteringHistory() {
        Cursor cursor = db.query("STATE",
                new String[]{"VOLTAGE", "CURRENT", "LEVEL"},
                null, null, null, null, "_id DESC", "30");
        GridLayout gridLayout = findViewById(R.id.metering_history);
        if (cursor.moveToLast()) {
            while (cursor.moveToPrevious()) {
                addToHistory(cursor, gridLayout);
            }
        }
        cursor.close();
    }

    public void reset(View view) {
        db.execSQL("delete from STATE");
        historyRecordsSize = 0;
        GridLayout gridLayout = findViewById(R.id.metering_history);
        gridLayout.removeAllViews();
    }

    public static boolean isCreated() {
        return created;
    }

}
