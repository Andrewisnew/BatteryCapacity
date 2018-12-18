package com.hfad.batterycapacity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final TextView voltageValView = findViewById(R.id.voltage_value);
        final TextView currentValView = findViewById(R.id.current_value);

        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                BatteryManager mBatteryManager = (BatteryManager)
                        getSystemService(BATTERY_SERVICE);

                double currentVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0;
                String currentVoltageStr = Double.toString(currentVoltage);
                voltageValView.setText(currentVoltageStr);

                double currentCurrent = mBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000.0;
                String currentCurrentStr = Double.toString(currentCurrent);
                currentValView.setText(currentCurrentStr);
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);

    }
}
