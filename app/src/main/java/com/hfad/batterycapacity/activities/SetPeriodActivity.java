package com.hfad.batterycapacity.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.model.Preferences;

public class SetPeriodActivity extends AppCompatActivity {
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_period);
        preferences = new Preferences(this);
    }

    public void setPeriod(View view) {

        EditText periodEditText = findViewById(R.id.period);

        if (periodEditText.getText().toString().isEmpty()){
            Toast toast = Toast.makeText(this, "Enter period", Toast.LENGTH_SHORT);
            toast.show();
        }else {
            int period = Integer.parseInt(periodEditText.getText().toString());
            preferences.savePeriod(period);

            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }
}
