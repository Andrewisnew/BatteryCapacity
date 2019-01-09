package com.hfad.batterycapacity;

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
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {

    public static final String ADD_STATE = "ADD_STATE";
    public static final String CAPACITY = "capacity";
    private int historyRecords;

    private SQLiteDatabase db;
    private int startLevel;
    private int curLevel;
    private static final int NUM_OF_METERINGS_PER_HOUR = 3600 / MainIntentService.PERIOD;
    private static final double NOMINAL_VOLTAGE = 3.8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            db = new BatteryCapacityDBHelper(this).getReadableDatabase();
        } catch (SQLiteException e) {
            Log.d("SQLiteException", e.getMessage());
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        addMeteringHistory();

        final TextView voltageValView = findViewById(R.id.voltage_value);
        final TextView currentValView = findViewById(R.id.current_value);
        final TextView levelValView = findViewById(R.id.level_value);
        final TextView capacityValView = findViewById(R.id.capacity_value);

        BroadcastReceiver br = new BroadcastReceiver() {
            @SuppressLint("DefaultLocale")
            public void onReceive(Context context, Intent intent) {
                Cursor cursor = db.query("STATE",
                        new String[]{"VOLTAGE", "CURRENT", "LEVEL"},
                        null, null, null, null, null);
                if (cursor.moveToLast()) {
                    curLevel = cursor.getInt(2);
                    voltageValView
                            .setText(String.format("%.2f", cursor.getDouble(0)));
                    currentValView
                            .setText(String.format("%.2f", cursor.getDouble(1)));
                    levelValView.setText(String.valueOf(curLevel));

                    GridLayout gridLayout = findViewById(R.id.metering_history);
                    addToHistory(cursor, gridLayout);

                    if (cursor.moveToFirst()) {
                        int capacity;
                        double sumOfPowers = 0;
                        startLevel = cursor.getInt(2);
                        do {
                            sumOfPowers += cursor.getDouble(0) * cursor.getDouble(1);
                        } while (cursor.moveToNext());

                        if (startLevel != curLevel) {
                            capacity = (int) (sumOfPowers / (NOMINAL_VOLTAGE * NUM_OF_METERINGS_PER_HOUR)
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
                    GridLayout.spec(historyRecords)
            );
            gridLayout.addView(textView, layoutParams);
        }
        historyRecords++;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (db != null) {
            db.close();
        }
    }

    @SuppressLint("DefaultLocale")
    private void addMeteringHistory() {
        Cursor cursor = db.query("STATE",
                new String[]{"VOLTAGE", "CURRENT", "LEVEL"},
                null, null, null, null, "_id DESC", "100");
        GridLayout gridLayout = findViewById(R.id.metering_history);
        if (cursor.moveToLast()) {
            while (cursor.moveToPrevious()) {
                addToHistory(cursor, gridLayout);
            }
        }
        cursor.close();
    }

}
