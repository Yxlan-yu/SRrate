package com.yxlanyu.refreshrate.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
public class RemoteKeepAliveService extends Service {
    private static final String CHANNEL_ID = "remote_keepalive_channel";
    private static final int NOTIFICATION_ID = 1007;
    public static void start(Context c) {
        try {
            Intent i = new Intent(c, RemoteKeepAliveService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) c.startForegroundService(i);
            else c.startService(i);
        } catch (Throwable ignored) {}
    }
    public static void stop(Context c) {
        try { c.stopService(new Intent(c, RemoteKeepAliveService.class)); } catch (Throwable ignored) {}
    }
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(CHANNEL_ID, "远程连接保持", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(ch);
        }
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            Notification.Builder b = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ? new Notification.Builder(this, CHANNEL_ID) : new Notification.Builder(this);
            b.setContentTitle("远程刷新率").setContentText("正在保持设备连接").setSmallIcon(android.R.drawable.stat_sys_data_bluetooth).setOngoing(true);
            startForeground(NOTIFICATION_ID, b.build());
        } catch (Throwable ignored) {}
        return START_STICKY;
    }
    @Override
    public IBinder onBind(Intent intent) { return null; }
}
