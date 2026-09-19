package com.yxlanyu.refreshrate.util;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.util.Log;
import android.view.Display;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.service.OverclockService;
import java.util.*;
public class AutoOverclockManager {
    private static final String TAG = "AutoOverclock";
    private static final long STEP_DELAY_MS = 800;
    private static final long POLL_DELAY_MS = 2000;
    private static Thread  daemonThread;
    private static volatile boolean running  = false;
    private static volatile String lastLog = "";
    private static volatile int    targetW   = 0;
    private static volatile int    targetH   = 0;
    private static volatile int    targetHz  = 0;
    private static volatile String authMode  = "";
    private static volatile String customOverrideRes = "";
    private static volatile int    customOverrideHz  = -1;
    private static Context appContext;
    private static Context getLocalizedContext() {
        if (appContext == null) return null;
        try {
            SharedPreferences prefs = appContext.getSharedPreferences("s", android.content.Context.MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(appContext.getResources().getConfiguration());
            config.setLocale(locale);
            return appContext.createConfigurationContext(config);
        } catch (Exception e) { return appContext;
        }
    }
    public static String getLastLog()  { return lastLog;
    }
    public static boolean isRunning()  { return running;
    }
    public static void startService(Context ctx, String mode,
                                    int tw, int th, int hz) {
        if (running) return;
        Intent intent = new Intent(ctx, OverclockService.class);
        intent.putExtra("auth_mode", mode);
        intent.putExtra("targetW", tw);
        intent.putExtra("targetH", th);
        intent.putExtra("targetHz", hz);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ctx.startForegroundService(intent);
        else
            ctx.startService(intent);
    }
    public static void stopService(Context ctx) {
        stop();
        ctx.stopService(new Intent(ctx, OverclockService.class));
    }
    public static void updateTarget(int tw, int th, int hz) {
        targetW  = tw;
        targetH  = th;
        targetHz = hz;
        if (appContext != null) lastLog = getLocalizedContext().getString(R.string.guard_target_updated, tw + "×" + th, hz);
        Log.d(TAG, lastLog);
    }
    public static void setCustomOverride(String res, int hz) {
        if (res == null || res.isEmpty() || hz <= 0) {
            clearCustomOverride();
            return;
        }
        customOverrideRes = res;
        customOverrideHz  = hz;
        if (appContext != null) {
            lastLog = getLocalizedContext().getString(R.string.guard_target_updated, res.replace("x", "×"), hz);
            Log.d(TAG, lastLog);
            if (running) OverclockService.updateNotification(appContext);
        }
    }
    public static void clearCustomOverride() {
        if (customOverrideRes.isEmpty() && customOverrideHz == -1) return;
        customOverrideRes = "";
        customOverrideHz  = -1;
        if (appContext != null && running) OverclockService.updateNotification(appContext);
    }
    public static void start(Context ctx, String mode,
                             int tw, int th, int hz) {
        if (running) return;appContext = ctx.getApplicationContext();
        authMode = mode;
        targetW  = tw;
        targetH  = th;
        targetHz = hz;
        running  = true;
        lastLog  = getLocalizedContext().getString(R.string.guard_start_format, tw + "×" + th, hz);
        daemonThread = new Thread(() -> {
            while (running) {
                try {
                    int curTargetW  = targetW;
                    int curTargetH  = targetH;
                    int curTargetHz = targetHz;
                    String ovrRes = customOverrideRes;
                    int ovrHz = customOverrideHz;
                    if (ovrRes != null && !ovrRes.isEmpty() && ovrHz > 0) {
                        String[] ovrWh = ovrRes.split("x");
                        if (ovrWh.length == 2) {
                            try {
                                curTargetW = Integer.parseInt(ovrWh[0].trim());
                                curTargetH = Integer.parseInt(ovrWh[1].trim());
                                curTargetHz = ovrHz;
                            } catch (Exception ignored) {}
                        }
                    }
                    List<DisplayMode> all = getSupportedModes(ctx);
                    List<DisplayMode> filtered = new ArrayList<>();
                    for (DisplayMode m : all) {
                        if (m.getWidth() == curTargetW && m.getHeight() == curTargetH)
                            filtered.add(m);
                    }
                    if (filtered.isEmpty()) {
                        lastLog = getLocalizedContext().getString(R.string.guard_no_res_format, curTargetW + "×" + curTargetH);
                        Thread.sleep(POLL_DELAY_MS);
                        continue;
                    }
                    filtered.sort((a, b) -> Float.compare(a.getRefreshRate(), b.getRefreshRate()));
                    DisplayMode finalTarget = filtered.get(filtered.size() - 1);
                    for (DisplayMode m : filtered) {
                        if (m.getRateInt() == curTargetHz) { finalTarget = m; break; }
                    }
                    float curRate = getCurrentRate(ctx);
                    int   curInt  = Math.round(curRate);
                    String curRes = getCurrentResolution(ctx);
                    String tgtRes = curTargetW + "×" + curTargetH;
                    boolean resOk  = tgtRes.equals(curRes);
                    boolean rateOk = (curInt == finalTarget.getRateInt());
                    if (resOk && rateOk) {
                        lastLog = getLocalizedContext().getString(R.string.guard_watching_format, finalTarget.getRateInt());
                        OverclockService.updateNotification(ctx);
                        Thread.sleep(POLL_DELAY_MS);
                        continue;
                    }
                    int curIdx = 0;
                    if (resOk) {
                        for (int i = filtered.size() - 1; i >= 0; i--) {
                            if (filtered.get(i).getRateInt() <= curInt) {
                             curIdx = i; break;
                            }
                        }
                    }
                    DisplayMode next;
                    if (curInt < finalTarget.getRateInt()) {
                        int nextIdx = Math.min(curIdx + 1, filtered.size() - 1);
                        while (nextIdx < filtered.size() - 1 && filtered.get(nextIdx).getRateInt() > finalTarget.getRateInt())
                            nextIdx--;
                        next = filtered.get(nextIdx);
                        String prefix = !resOk ? getLocalizedContext().getString(R.string.guard_res_fix) : "";
                        lastLog = getLocalizedContext().getString(R.string.guard_hz_up, prefix, curInt, next.getRateInt());
                    } else {
                        int nextIdx = Math.max(curIdx - 1, 0);
                        while (nextIdx > 0 && filtered.get(nextIdx).getRateInt() < finalTarget.getRateInt())
                            nextIdx++;
                        next = filtered.get(nextIdx);
                        lastLog = getLocalizedContext().getString(R.string.guard_hz_down, curInt, next.getRateInt());
                    }
                    Log.d(TAG, lastLog);
                    if ("root".equals(authMode)) {
                        RootUtils.setDisplayMode(next.getWidth(), next.getHeight(), next.getRateInt(), next.getSfIndex());
                    } else if ("shizuku".equals(authMode)) {
                        ShizukuUtils.setDisplayMode(next.getWidth(), next.getHeight(), next.getRateInt(), next.getSfIndex());
                    }
                    OverclockService.updateNotification(ctx);
                    Thread.sleep(STEP_DELAY_MS);
                } catch (InterruptedException e) {
                    break;
                } catch (Exception e) {
                    Log.e(TAG, "daemon: " + e.getMessage());
                    try { Thread.sleep(POLL_DELAY_MS); } catch (InterruptedException ex) { break; }
                }
            }
            running = false;
            if (appContext != null) lastLog = getLocalizedContext().getString(R.string.guard_stopped);
        });
        daemonThread.setDaemon(true);
        daemonThread.start();
    }
    public static void stop() {
        running = false;
        if (daemonThread != null) { daemonThread.interrupt(); daemonThread = null; }
    }
    public static float getCurrentRate(Context ctx) {
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            return dm.getDisplay(Display.DEFAULT_DISPLAY).getRefreshRate();
        } catch (Exception e) { return 0f; }
    }
    public static String getCurrentResolution(Context ctx) {
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            Display.Mode m = dm.getDisplay(Display.DEFAULT_DISPLAY).getMode();
            return m.getPhysicalWidth() + "×" + m.getPhysicalHeight();
        } catch (Exception e) { return "--";
        }
    }
    public static List<DisplayMode> getSupportedModes(Context ctx) {
        List<DisplayMode> list = new ArrayList<>();
        try {
            DisplayManager dm = (DisplayManager) ctx.getSystemService(Context.DISPLAY_SERVICE);
            Display.Mode[] modes = dm.getDisplay(Display.DEFAULT_DISPLAY).getSupportedModes();
            int sfIdx = 0;
            for (Display.Mode m : modes) {
                DisplayMode dmode = new DisplayMode(m.getPhysicalWidth(), m.getPhysicalHeight(),
                        m.getRefreshRate(), m.getModeId());
                dmode.setSfIndex(sfIdx++);
                list.add(dmode);
            }
        } catch (Exception ignored) {}
        return list;
    }
}
