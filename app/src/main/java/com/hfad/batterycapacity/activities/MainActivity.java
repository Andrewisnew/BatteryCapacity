package com.hfad.batterycapacity.activities;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;

import com.hfad.batterycapacity.entities.BatteryState;
import com.hfad.batterycapacity.entities.MeteringResult;
import com.hfad.batterycapacity.model.db.BatteryStateDBHelper;
import com.hfad.batterycapacity.model.db.MeteringResultDBHelper;
import com.hfad.batterycapacity.services.MainIntentService;
import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.model.Preferences;

import java.util.List;


public class MainActivity extends Activity {

    public static final String LOG = "LOG";
    private static final int LIMIT_METERING_HISTORY = 30;
    private static boolean created;

    private int historyRecordsSize;
    private BatteryStateDBHelper batteryStateDBHelper;
    private MeteringResultDBHelper meteringResultDBHelper;

    private AddedBatteryStateReceiver addedBatteryStateReceiver;
    private RemovedMeteringHistoryBroadcastReceiver removedMeteringHistoryBroadcastReceiver;
    private AddedMeteringResultBroadcastReceiver addedMeteringResultBroadcastReceiver;
    public static int NUM_OF_METERINGS_PER_HOUR;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG, getClass().toString() + " onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        created = true;

        preferences = new Preferences(this);
        batteryStateDBHelper = new BatteryStateDBHelper(this);
        meteringResultDBHelper = new MeteringResultDBHelper(this);

        int period = preferences.loadPeriod();
        if (period == -1) {
            Intent intent = new Intent(this, SetPeriodActivity.class);
            startActivity(intent);
        } else {
            NUM_OF_METERINGS_PER_HOUR = 3600 / period;
            TextView capacityValView = findViewById(R.id.capacity_value);
            computeAndSaveCapacity();
            int capacity = preferences.loadCapacity();
            if (capacity == -1) {
                capacityValView.setText("not computed");
            } else {
                capacityValView.setText(String.valueOf(capacity));
            }

            addMeteringHistory();

            addedBatteryStateReceiver = new AddedBatteryStateReceiver();
            registerReceiver(addedBatteryStateReceiver, new IntentFilter(AddedBatteryStateReceiver.ACTION));
            removedMeteringHistoryBroadcastReceiver = new RemovedMeteringHistoryBroadcastReceiver();
            registerReceiver(removedMeteringHistoryBroadcastReceiver, new IntentFilter(RemovedMeteringHistoryBroadcastReceiver.ACTION));
            addedMeteringResultBroadcastReceiver = new AddedMeteringResultBroadcastReceiver();
            registerReceiver(addedMeteringResultBroadcastReceiver, new IntentFilter(AddedMeteringResultBroadcastReceiver.ACTION));
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
        if (meteringResultDBHelper != null) {
            meteringResultDBHelper.close();
        }
        if(addedBatteryStateReceiver != null){
            unregisterReceiver(addedBatteryStateReceiver);
        }
        if(removedMeteringHistoryBroadcastReceiver != null){
            unregisterReceiver(removedMeteringHistoryBroadcastReceiver);
        }
        if(addedMeteringResultBroadcastReceiver != null){
            unregisterReceiver(addedMeteringResultBroadcastReceiver);
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

    public void toMeteringResults(View view) {
        Intent intent = new Intent(this, MeteringResultsActivity.class);
        startActivity(intent);
    }

    public class AddedBatteryStateReceiver extends BroadcastReceiver {
        public static final String ACTION = "ADD_STATE";

        @Override
        public void onReceive(Context context, Intent intent) {
            List<BatteryState> states = batteryStateDBHelper.getLast(LIMIT_METERING_HISTORY);
            if (!states.isEmpty()) {
                int curLevel = states.get(states.size() - 1).getLevel();
                GridLayout gridLayout = findViewById(R.id.metering_history);
                if (historyRecordsSize >= 30) {
                    gridLayout.removeAllViews();

                    historyRecordsSize = 0;
                    for (int i = 0; i < states.size(); i++) {
                        addToHistory(states.get(i), gridLayout);
                    }
                } else {
                    addToHistory(states.get(states.size() - 1), gridLayout);
                }
            }
        }
    }

    public class RemovedMeteringHistoryBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION = "REMOVE_METERING_HISTORY";

        @Override
        public void onReceive(Context context, Intent intent) {
            GridLayout gridLayout = findViewById(R.id.metering_history);
            gridLayout.removeAllViews();
            historyRecordsSize = 0;
        }
    }

    public class AddedMeteringResultBroadcastReceiver extends BroadcastReceiver {
        public static final String ACTION = "ADDED_METERING_RESULT";

        @Override
        public void onReceive(Context context, Intent intent) {
            computeAndSaveCapacity();
        }
    }

    //sum(P) / (avg(V) * Nh) * 100 / (sum(dif(L)))
    private boolean computeAndSaveCapacity() {
        List<MeteringResult> meteringResults = meteringResultDBHelper.getAll();
        if (meteringResults.isEmpty()) {
            return false;
        }
        double totalPower = 0;
        double totalVoltage = 0;
        int totalLevelDifference = 0;
        for (MeteringResult meteringResult : meteringResults) {
            totalPower += meteringResult.getSumOfPowers();
            totalVoltage += meteringResult.getAvgVoltage();
            totalLevelDifference += meteringResult.getFinishLevel() - meteringResult.getStartLevel();
        }
        double avgVoltage = totalVoltage / meteringResults.size();
        int capacity = (int) (totalPower / (avgVoltage * NUM_OF_METERINGS_PER_HOUR)
                * 100 / (totalLevelDifference) * 1000);
        preferences.saveCapacity(capacity);
        return true;
    }
}
