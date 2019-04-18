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

import com.hfad.batterycapacity.entities.BatteryState;
import com.hfad.batterycapacity.model.db.BatteryCapacityDBHelper;
import com.hfad.batterycapacity.model.db.BatteryStateDBHelper;
import com.hfad.batterycapacity.services.MainIntentService;
import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.model.Preferences;

import java.util.Collections;
import java.util.List;


public class MainActivity extends Activity {

    public static final String ADD_STATE = "ADD_STATE";
    public static final String LOG = "LOG";
    private static final int LIMIT_METERING_HISTORY = 30;
    private static boolean created;

    private int historyRecordsSize;
    private BatteryStateDBHelper batteryStateDBHelper;
    private int startLevel;
    private int curLevel;
    private static int NUM_OF_METERINGS_PER_HOUR;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG, getClass().toString() + " onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        created = true;

        preferences = new Preferences(this);
        batteryStateDBHelper = new BatteryStateDBHelper(this);

        int period = preferences.loadPeriod();
        if (period == -1) {
            Intent intent = new Intent(this, SetPeriodActivity.class);
            startActivity(intent);
        } else {
            NUM_OF_METERINGS_PER_HOUR = 3600 / period;

            addMeteringHistory();

            final TextView capacityValView = findViewById(R.id.capacity_value);

            BroadcastReceiver br = new BroadcastReceiver() {
                @SuppressLint("DefaultLocale")
                public void onReceive(Context context, Intent intent) {
                    List<BatteryState> states = batteryStateDBHelper.getLast(LIMIT_METERING_HISTORY);
                    if (!states.isEmpty()) {
                        curLevel = states.get(states.size() - 1).getLevel();
                        GridLayout gridLayout = findViewById(R.id.metering_history);
                        if (historyRecordsSize >= 30) {
                            gridLayout.removeAllViews();

                            historyRecordsSize = 0;
                            System.err.println("SIZEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEEE" +  states.size());
                            for (int i = 0; i < states.size(); i++) {
                                System.out.println(states.get(i));
                                addToHistory(states.get(i), gridLayout);
                            }
                        } else {
                            addToHistory(states.get(states.size() - 1), gridLayout);
                            System.out.println(states.get(states.size() - 1));
                        }
                    }else {
                        System.err.println(states.size() + " FAIL");
                    }
                    List<BatteryState> allStates = batteryStateDBHelper.getAll();

                    if (!allStates.isEmpty()) {

                        startLevel = allStates.get(0).getLevel();

                        if (startLevel != curLevel) {
                            int capacity;
                            double sumOfPowers = 0;
                            for (BatteryState state : allStates) {
                                sumOfPowers += state.getCurrent() * state.getVoltage();
                            }
                            capacity = (int) (sumOfPowers / (batteryStateDBHelper.getAvgVoltage() * NUM_OF_METERINGS_PER_HOUR)
                                    * 100 / (curLevel - startLevel) * 1000);
                            capacityValView.setText(String.valueOf(capacity));
                        } else {
                            capacityValView.setText("-");
                            Toast toast = Toast.makeText(MainActivity.this, "s="+startLevel +" c="+curLevel
                                    , Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                }
            };
            registerReceiver(br, new IntentFilter(ADD_STATE));
            Intent intent = new Intent(this, MainIntentService.class);
            startService(intent);
        }
    }


    @SuppressLint("DefaultLocale")
    private void addToHistory(BatteryState state, GridLayout gridLayout) {
        for (int i = 0; i < 3; i++) {
            TextView textView = new TextView(this);
            textView.setPadding(5, 0, 5, 0);
            textView.setGravity(Gravity.CENTER);
            switch (i) {
                case 0:
                    textView.setText(String.format("%.2f", state.getVoltage()));
                    break;
                case 1:
                    textView.setText(String.format("%.2f", state.getCurrent()));
                    break;
                case 2:
                    textView.setText(String.valueOf(state.getLevel()));
                    break;
            }
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
        if (batteryStateDBHelper != null) {
            batteryStateDBHelper.close();
        }
    }

    @SuppressLint("DefaultLocale")
    private void addMeteringHistory() {
        List<BatteryState> states = batteryStateDBHelper.getLast(LIMIT_METERING_HISTORY);
        GridLayout gridLayout = findViewById(R.id.metering_history);
        for (BatteryState state : states) {
            addToHistory(state, gridLayout);
        }
    }

    public void reset(View view) {
        batteryStateDBHelper.deleteAll();
        historyRecordsSize = 0;
        GridLayout gridLayout = findViewById(R.id.metering_history);
        gridLayout.removeAllViews();
    }

    public static boolean isCreated() {
        return created;
    }

}
