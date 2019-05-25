package batterycapacity.periodanalysis.services;

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
import android.widget.Toast;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.TimeUnit;

import batterycapacity.periodanalysis.R;
import batterycapacity.periodanalysis.activities.MainActivity;
import batterycapacity.periodanalysis.entities.BatteryState;
import batterycapacity.periodanalysis.model.Preferences;
import batterycapacity.periodanalysis.model.db.BatteryStateDBHelper;

public class MainIntentService extends IntentService {
    private static final String LOG = "LOG";
    private BatteryStateDBHelper batteryStateDBHelper;
    public static final int MAX = 300;
    private double curCurrent;
    private double curVoltage;
    private int curLevel = -1;
    private int startLevel;

    public MainIntentService() {
        super("MainIntentService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(LOG, getClass().toString() + " onCreate");
        batteryStateDBHelper = new BatteryStateDBHelper(this);

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
        startForeground(143, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(LOG, getClass().toString() + " onDestroy");
        if (batteryStateDBHelper != null) {
            batteryStateDBHelper.close();
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Preferences preferences = new Preferences(this);
        registerBatteryReceiver();
        for (long i = 1; ; i++) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Log.i(LOG, getClass().toString() + " : " + curCurrent);

            for (int j = 1; j <= MAX; j++) {
                if (i % j == 0) {
                    //System.out.print(j + " ");
                    if(preferences.loadLevel() >= curLevel){
                        preferences.saveLevel(curLevel);
                        batteryStateDBHelper.insert(new BatteryState(curVoltage, curCurrent, curLevel, j));
                    }
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
                if (curLevel == -1) {
                    startLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                    curLevel = startLevel;
                } else {
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

                Toast.makeText(context, String.valueOf(curCurrent), Toast.LENGTH_SHORT).show();
            }
        };
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryReceiver, filter);
    }
}
