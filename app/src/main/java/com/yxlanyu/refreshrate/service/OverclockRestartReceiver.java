package com.yxlanyu.refreshrate.service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
public class OverclockRestartReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = context.getSharedPreferences("s", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("auto_overclock", false)) return;
        String authMode = prefs.getString("auth_mode", "");
        String ocRes    = prefs.getString("oc_target_res", "");
        int    ocHz     = prefs.getInt("oc_target_hz", -1);
        if (authMode.isEmpty() || ocRes.isEmpty() || ocHz < 0) return;
        String[] wh = ocRes.split("x");
        if (wh.length < 2) return;
        try {
            int tw = Integer.parseInt(wh[0]);
            int th = Integer.parseInt(wh[1]);
            Intent si = new Intent(context, OverclockService.class);
            si.putExtra("auth_mode", authMode);
            si.putExtra("targetW", tw);
            si.putExtra("targetH", th);
            si.putExtra("targetHz", ocHz);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                context.startForegroundService(si);
            else
                context.startService(si);
        } catch (Exception ignored) {}
    }
}
