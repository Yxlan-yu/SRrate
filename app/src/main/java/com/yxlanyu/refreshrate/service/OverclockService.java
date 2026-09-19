package com.yxlanyu.refreshrate.service;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
public class OverclockService extends Service {
    private static final String CHANNEL_ID = "overclock_channel";
    private static final int NOTIFICATION_ID = 1001;
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && "STOP".equals(intent.getAction())) {
            AutoOverclockManager.stopService(this);
            return START_NOT_STICKY;
        }
        Context lc = getLocalizedContext(this);
        startForeground(NOTIFICATION_ID, buildNotification());
        String authMode = intent != null ? intent.getStringExtra("auth_mode") : "";
        int targetW = intent != null ? intent.getIntExtra("targetW", 0) : 0;
        int targetH = intent != null ? intent.getIntExtra("targetH", 0) : 0;
        int targetHz = intent != null ? intent.getIntExtra("targetHz", 0) : 0;
        AutoOverclockManager.start(this, authMode, targetW, targetH, targetHz);
        return START_STICKY;
    }
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        android.app.PendingIntent restartPi;
        Intent restartIntent = new Intent(getApplicationContext(), OverclockRestartReceiver.class);
        int flags = android.app.PendingIntent.FLAG_ONE_SHOT;
        if (android.os.Build.VERSION.SDK_INT >= 23)
            flags |= android.app.PendingIntent.FLAG_IMMUTABLE;
        restartPi = android.app.PendingIntent.getBroadcast(getApplicationContext(), 1, restartIntent, flags);
        android.app.AlarmManager am = (android.app.AlarmManager) getSystemService(ALARM_SERVICE);
        if (am != null)
            am.set(android.app.AlarmManager.ELAPSED_REALTIME,
                    android.os.SystemClock.elapsedRealtime() + 1000, restartPi);
        super.onTaskRemoved(rootIntent);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        AutoOverclockManager.stop();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    private static Context getLocalizedContext(Context ctx) {
        try {
            SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(ctx.getResources().getConfiguration());
            config.setLocale(locale);
            return ctx.createConfigurationContext(config);
        } catch (Exception e) { return ctx;
        }
    }
    private void createNotificationChannel() {
    Context lc = getLocalizedContext(this);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    lc.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private Notification buildNotification() {
    Context lc = getLocalizedContext(this);
    Intent stopIntent = new Intent(this, OverclockService.class);
    stopIntent.setAction("STOP");
    int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, pendingFlags);
    Intent openIntent = new Intent(this, com.yxlanyu.refreshrate.MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    int openFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) openFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent openPendingIntent = PendingIntent.getActivity(this, 2, openIntent, openFlags);
    return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(lc.getString(R.string.notification_title))
            .setContentText(AutoOverclockManager.getLastLog())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.notification_stop), stopPendingIntent)
            .setOngoing(true)
            .build();
    }
    public static void updateNotification(Context context) {
    Context lc = getLocalizedContext(context);
    NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (nm == null) return;
    Intent stopIntent = new Intent(context, OverclockService.class);
    stopIntent.setAction("STOP");
    int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent stopPendingIntent = PendingIntent.getService(context, 0, stopIntent, pendingFlags);
    Intent openIntent = new Intent(context, com.yxlanyu.refreshrate.MainActivity.class);
    openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    int openFlags = PendingIntent.FLAG_UPDATE_CURRENT;
    if (Build.VERSION.SDK_INT >= 31) openFlags |= PendingIntent.FLAG_IMMUTABLE;
    PendingIntent openPendingIntent = PendingIntent.getActivity(context, 2, openIntent, openFlags);
    Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(lc.getString(R.string.notification_title))
            .setContentText(AutoOverclockManager.getLastLog())
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, lc.getString(R.string.notification_stop), stopPendingIntent)
            .setOngoing(true)
            .build();
    nm.notify(NOTIFICATION_ID, notification);
}
}
