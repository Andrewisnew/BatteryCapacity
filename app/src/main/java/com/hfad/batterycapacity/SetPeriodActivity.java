package com.hfad.batterycapacity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class SetPeriodActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_period);
    }

    public void setPeriod(View view) {

        EditText period = findViewById(R.id.period);

        if (period.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Enter period", Toast.LENGTH_SHORT);
            toast.show();
        }else {
            MainIntentService.PERIOD = Integer.parseInt(period.getText().toString());
            try {
                SQLiteDatabase db = new BatteryCapacityDBHelper(this).getWritableDatabase();
                ContentValues stateValues = new ContentValues();
                stateValues.put("NAME", "period");
                stateValues.put("VAL", Integer.parseInt(period.getText().toString()));
                db.insert("VALS", null, stateValues);
            } catch (SQLiteException e) {
                Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
                toast.show();
            }
            MainActivity.setPeriodIsSet(true);
            Intent intent = new Intent(this, MainActivity.class);

            startActivity(intent);
        }
    }
}
