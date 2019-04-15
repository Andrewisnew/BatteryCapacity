package com.hfad.batterycapacity.config;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hfad.batterycapacity.services.MainIntentService;

public class AutorunBroadcastReceiver extends BroadcastReceiver {
    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    public void onReceive(Context context, Intent intent) {
        context.startService(new Intent(context, MainIntentService.class));
    }
}
