package com.hfad.batterycapacity.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.entities.MeteringResult;
import com.hfad.batterycapacity.model.db.MeteringResultDBHelper;

import java.util.List;

public class MeteringResultsActivity extends Activity {

    public static final String LOG = "LOG";
    MeteringResultDBHelper meteringResultDBHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_metering_results);
        Log.i(LOG, getClass().toString() + " onCreate");
        meteringResultDBHelper = new MeteringResultDBHelper(this);

        addResults();

    }

    @SuppressLint("DefaultLocale")
    private void addResults() {
        List<MeteringResult> meteringResults = meteringResultDBHelper.getAll();
        GridLayout gridLayout = findViewById(R.id.metering_results_table);
        int meteringResultsSize = 1;
        for (MeteringResult meteringResult : meteringResults) {
            for (int i = 0; i < 5; i++) {
                TextView textView = new TextView(this);
                textView.setPadding(5, 0, 5, 0);
                textView.setGravity(Gravity.CENTER);
                switch (i) {
                    case 0:
                        textView.setText(String.format("%.2f", meteringResult.getSumOfPowers()));
                        break;
                    case 1:
                        textView.setText(String.format("%.2f", meteringResult.getAvgVoltage()));
                        break;
                    case 2:
                        textView.setText(String.valueOf(meteringResult.getStartLevel()));
                        break;
                    case 3:
                        textView.setText(String.valueOf(meteringResult.getFinishLevel()));
                        break;
                    case 4:
                        textView.setText(String.valueOf(computeAndGetCapacity(meteringResult)));

                }
                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                        GridLayout.spec(meteringResultsSize),
                        GridLayout.spec(i)
                );
                gridLayout.addView(textView, layoutParams);
            }
            meteringResultsSize++;
        }

    }

    private int computeAndGetCapacity(MeteringResult meteringResult) {

        return  (int) (meteringResult.getSumOfPowers() / (meteringResult.getAvgVoltage() * MainActivity.NUM_OF_METERINGS_PER_HOUR)
                * 100 / (meteringResult.getFinishLevel() - meteringResult.getStartLevel()) * 1000);

    }


}
