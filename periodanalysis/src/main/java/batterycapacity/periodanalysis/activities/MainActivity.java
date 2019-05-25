package batterycapacity.periodanalysis.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

import batterycapacity.periodanalysis.R;
import batterycapacity.periodanalysis.entities.BatteryState;
import batterycapacity.periodanalysis.model.db.BatteryStateDBHelper;
import batterycapacity.periodanalysis.services.MainIntentService;

public class MainActivity extends Activity {

    public static final String LOG = "LOG";

    private static final int interval = 5;
    private BatteryStateDBHelper batteryStateDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG, getClass().toString() + " onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        batteryStateDBHelper = new BatteryStateDBHelper(this);


        LinearLayout linearLayout = findViewById(R.id.linearLayout);
        for (int i = 1; i <= MainIntentService.MAX; i++) {
            TextView textView = new TextView(this);
            textView.setText(i + ")");
            linearLayout.addView(textView);
            linearLayout.addView(getResults(i));
        }

        Intent intent = new Intent(this, MainIntentService.class);
        startService(intent);
    }

    private HorizontalScrollView getResults(int period) {
        HorizontalScrollView scrollView = new HorizontalScrollView(this);

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setRowCount(2);
        List<Integer> capacities = getCapacities(period);

        if(capacities.isEmpty()){
            return scrollView;
        }

        for (int j = 0; j < capacities.size(); j++) {
            for (int i = 0; i < 2; i++) {
                TextView textView = new TextView(this);
                textView.setPadding(5, 0, 5, 0);

                if(i == 0){
                    textView.setText(String.valueOf((j+1) * interval));
                } else {
                    textView.setText(String.valueOf(capacities.get(j)));
                }

                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                        GridLayout.spec(i),
                        GridLayout.spec(j)
                );
                gridLayout.addView(textView, layoutParams);
            }
        }


        scrollView.addView(gridLayout);
        return scrollView;
    }

    private List<Integer> getCapacities(int period) {
        List<BatteryState> results = batteryStateDBHelper.getByPeriod(period);
        if(results.isEmpty()){
            return new ArrayList<>();
        }
        int maxLevel = results.get(0).getLevel();
        int bound = interval;
        List<Integer> capacities = new ArrayList<>();
        double totalPower = 0;
        double totalVoltage = 0;
        int numOfResultsInInterval = 0;
        for(BatteryState state : results){
            if(maxLevel - state.getLevel() >= bound){
                double avgVoltage = totalVoltage / numOfResultsInInterval;
                int capacity = (int) (totalPower / (avgVoltage * 3600 / period)
                * 100 / bound * 1000);
                bound += interval;
                capacities.add(-capacity);
            }

            totalPower += state.getCurrent() * state.getVoltage();
            totalVoltage += state.getVoltage();
            numOfResultsInInterval++;
        }

        return capacities;
    }


//    @SuppressLint("DefaultLocale")
//    private void addToHistory(BatteryState state, GridLayout gridLayout) {
//        for (int i = 0; i < 3; i++) {
//            TextView textView = new TextView(this);
//            textView.setPadding(5, 0, 5, 0);
//            textView.setGravity(Gravity.CENTER);
//            switch (i) {
//                case 0:
//                    textView.setText(String.format("%.2f", state.getVoltage()));
//                    break;
//                case 1:
//                    textView.setText(String.format("%.2f", state.getCurrent()));
//                    break;
//                case 2:
//                    textView.setText(String.valueOf(state.getLevel()));
//                    break;
//            }
//            GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
//                    GridLayout.spec(i),
//                    GridLayout.spec(historyRecordsSize)
//            );
//            gridLayout.addView(textView, layoutParams);
//        }
//        historyRecordsSize++;
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (batteryStateDBHelper != null) {
            batteryStateDBHelper.close();
        }

    }

//    @SuppressLint("DefaultLocale")
//    private void addMeteringHistory() {
//        List<BatteryState> states = batteryStateDBHelper.getLast(LIMIT_METERING_HISTORY);
//        GridLayout gridLayout = findViewById(R.id.metering_history);
//        for (BatteryState state : states) {
//            addToHistory(state, gridLayout);
//        }
//    }


//    //sum(P) / (avg(V) * Nh) * 100 / (sum(dif(L)))
//    private boolean computeAndSaveCapacity() {
//        List<MeteringResult> meteringResults = meteringResultDBHelper.getAll();
//        if (meteringResults.isEmpty()) {
//            return false;
//        }
//        double totalPower = 0;
//        double totalVoltage = 0;
//        int totalLevelDifference = 0;
//        for (MeteringResult meteringResult : meteringResults) {
//            totalPower += meteringResult.getSumOfPowers();
//            totalVoltage += meteringResult.getAvgVoltage();
//            totalLevelDifference += meteringResult.getFinishLevel() - meteringResult.getStartLevel();
//        }
//        double avgVoltage = totalVoltage / meteringResults.size();
//        int capacity = (int) (totalPower / (avgVoltage * NUM_OF_METERINGS_PER_HOUR)
//                * 100 / (totalLevelDifference) * 1000);
//        preferences.saveCapacity(capacity);
//        return true;
//    }
}