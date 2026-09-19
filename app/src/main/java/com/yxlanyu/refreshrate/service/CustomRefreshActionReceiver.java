package com.yxlanyu.refreshrate.service;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
public class CustomRefreshActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null || !"STOP_CUSTOM_REFRESH".equals(intent.getAction())) return;
        SharedPreferences prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.edit().putBoolean("custom_app_refresh", false).apply();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(1002);
    }
}
