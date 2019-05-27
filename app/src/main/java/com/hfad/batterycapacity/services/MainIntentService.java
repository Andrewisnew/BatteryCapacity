package com.hfad.batterycapacity.services;

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
import android.os.BatteryManager;
import android.util.Log;

import com.hfad.batterycapacity.entities.BatteryState;
import com.hfad.batterycapacity.entities.MeteringResult;
import com.hfad.batterycapacity.R;
import com.hfad.batterycapacity.activities.MainActivity;
import com.hfad.batterycapacity.model.Preferences;
import com.hfad.batterycapacity.model.db.BatteryStateDBHelper;
import com.hfad.batterycapacity.model.db.MeteringResultDBHelper;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

public class MainIntentService extends IntentService {
    private static final String LOG = "LOG";
    private Preferences preferences;
    private BatteryStateDBHelper batteryStateDBHelper;
    private MeteringResultDBHelper meteringResultDBHelper;

    private double curCurrent;
    private double curVoltage;
    private int curLevel = -1;
    private int startLevel = -1;

    public MainIntentService() {
        super("MainIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG, getClass().toString() + " onCreate");
        preferences = new Preferences(this);
        batteryStateDBHelper = new BatteryStateDBHelper(this);
        meteringResultDBHelper = new MeteringResultDBHelper(this);

        if(!batteryStateDBHelper.isEmpty()){
            computeMeteringResult();
        }
        createNotification();
    }

    private void createNotification() {
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
        Log.i(LOG, getClass().toString() + " onDestroy");
        if(batteryStateDBHelper != null){
            batteryStateDBHelper.close();
        }
        if(meteringResultDBHelper != null){
            meteringResultDBHelper.close();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        registerBatteryReceiver();
        int period = preferences.loadPeriod();
        while (true) {
            synchronized (this) {
                try {
                    TimeUnit.SECONDS.sleep(period);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.i(LOG, getClass().toString() + " : " + curCurrent);
            if(curCurrent >= 0){
                if(!batteryStateDBHelper.isEmpty()){
                    computeMeteringResult();
                }
            }else if (curLevel < startLevel){
                batteryStateDBHelper.insert(new BatteryState(curVoltage, curCurrent, curLevel));
                if(MainActivity.isCreated()) {
                    Intent capIntent = new Intent(MainActivity.AddedBatteryStateReceiver.ACTION);
                    sendBroadcast(capIntent);
                }
            }
        }
    }

    public void registerBatteryReceiver() {
        BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                curVoltage = intent
                        .getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000.0;
                if(startLevel == -1){
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

    private void computeMeteringResult() {
        MeteringResult meteringResult = batteryStateDBHelper.getMeteringResult();
        batteryStateDBHelper.deleteAll();
        if(MainActivity.isCreated()) {
            Intent removeMeterHistIntent = new Intent(MainActivity.RemovedMeteringHistoryBroadcastReceiver.ACTION);
            sendBroadcast(removeMeterHistIntent);
        }
        if(meteringResult.getStartLevel() != meteringResult.getFinishLevel()) {
            meteringResultDBHelper.insert(meteringResult);
            if(MainActivity.isCreated()) {
                Intent addedMeteringResultIntent = new Intent(MainActivity.RemovedMeteringHistoryBroadcastReceiver.ACTION);
                sendBroadcast(addedMeteringResultIntent);
            }
        }
    }
}
