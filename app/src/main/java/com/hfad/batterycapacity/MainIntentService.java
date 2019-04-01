package com.hfad.batterycapacity;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.BatteryManager;
import android.widget.Toast;

import com.hfad.batterycapacity.config.Preferences;

import java.io.IOException;
import java.io.RandomAccessFile;

public class MainIntentService extends IntentService {
    private Preferences preferences;

    private double curCurrent;
    private double curVoltage;
    private int curLevel = -1;
    private int startLevel;
    private SQLiteDatabase db;

    public MainIntentService() {
        super("MainIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        preferences = new Preferences(this);
        try {
            db = new BatteryCapacityDBHelper(this).getWritableDatabase();
        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        Intent intent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(intent);
        PendingIntent pendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = getString(R.string.app_name);
        Notification.Builder notificationBuilder;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(channelId);
            notificationChannel.setSound(null, null);

            notificationManager.createNotificationChannel(notificationChannel);

            notificationBuilder = new Notification.Builder(this, channelId);
        } else {
            notificationBuilder = new Notification.Builder(this);
        }

        Notification notification = notificationBuilder
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(getString(R.string.app_name))
                .setContentText("Calculation")
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_MAX)
                .build();
        startForeground (143, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(db != null){
            db.close();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        registerBatteryResiver();
        int period = preferences.loadPeriod();
        while (true) {
            synchronized (this) {
                try {
                    wait(period * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(db != null) {
                BatteryCapacityDBHelper.insert(db, curVoltage, curCurrent, curLevel);
            }
            if(MainActivity.isCreated()) {
                Intent capIntent = new Intent(MainActivity.ADD_STATE);
                sendBroadcast(capIntent);
            }
        }
    }

    public void registerBatteryResiver() {
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                curVoltage = intent
                        .getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0;
                if(curLevel == -1){
                    startLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    curLevel = startLevel;
                }else {
                    curLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    BatteryManager mBatteryManager = (BatteryManager)
                            getSystemService(BATTERY_SERVICE);
                    curCurrent = mBatteryManager
                            .getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) / 1000.0;
                } else {
                    try {
                        RandomAccessFile reader = new RandomAccessFile("/sys/class/power_supply/battery/current_now", "r");
                        String currentStr = reader.readLine();
                        curCurrent = Integer.parseInt(currentStr) / 1_000_000.0;
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }



}
