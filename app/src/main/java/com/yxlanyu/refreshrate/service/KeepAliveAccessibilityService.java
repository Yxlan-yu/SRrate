package com.yxlanyu.refreshrate.service;
import android.accessibilityservice.AccessibilityService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.RootUtils;
import com.yxlanyu.refreshrate.util.ShizukuUtils;
import java.util.List;
public class KeepAliveAccessibilityService extends AccessibilityService {
    private static final String TAG = "KeepAliveA11y";
    private static final int CUSTOM_NOTIF_ID = 1002;
    private static final long FG_DEBOUNCE_MS = 300;
    private String currentFgPackage = "";
    private volatile String pendingFgPackage = "";
    private volatile String lastAppliedConfig = "";  // "effectivePkg@res@hz"
    private volatile String lastRealPkg = "";
    private volatile String stickyPkg = "";     
    private android.os.Handler fgHandler;
    private Runnable pendingApplyRunnable;
    private SharedPreferences servicePrefs;
    private SharedPreferences.OnSharedPreferenceChangeListener prefListener;
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        fgHandler = new android.os.Handler(android.os.Looper.getMainLooper());
        Log.d(TAG, "无障碍服务已连接");
        servicePrefs = getSharedPreferences("s", MODE_PRIVATE);
        prefListener = (sp, key) -> {
            if (!"custom_app_refresh".equals(key)) return;
            if (sp.getBoolean("custom_app_refresh", false)) {
                if (lastRealPkg != null && !lastRealPkg.isEmpty()) updatePersistentNotification(lastRealPkg);
            } else {
                AutoOverclockManager.clearCustomOverride();
                lastAppliedConfig = "";
                cancelCustomNotification();
            }
        };
        servicePrefs.registerOnSharedPreferenceChangeListener(prefListener);
        checkAndRestartService();
    }
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;
        String pkg = getTopForegroundPackage(event);
        if (pkg == null || pkg.isEmpty()) return;
        if (isSystemUiOrSelf(pkg)) { return; }
        scheduleForegroundApply(pkg);
    }
    private String getTopForegroundPackage(AccessibilityEvent event) {
        try {
            java.util.List<android.view.accessibility.AccessibilityWindowInfo> windows = getWindows();
            if (windows != null && !windows.isEmpty()) {
                android.util.DisplayMetrics dm = getResources().getDisplayMetrics();
                float wpx = dm.widthPixels;
                float hpx = dm.heightPixels;
                for (android.view.accessibility.AccessibilityWindowInfo w : windows) {
                    if (w == null || w.getType() != android.view.accessibility.AccessibilityWindowInfo.TYPE_APPLICATION) continue;
                    android.graphics.Rect b = new android.graphics.Rect();
                    w.getBoundsInScreen(b);
                    if (b.width() < wpx * 0.90f || b.height() < hpx * 0.90f) continue;
                    android.view.accessibility.AccessibilityNodeInfo root = w.getRoot();
                    if (root != null) {
                        CharSequence p = root.getPackageName();
                        root.recycle();
                        if (p != null && p.length() > 0) return p.toString();
                    }
                }
                return "";
            }
        } catch (Exception ignored) {}
        CharSequence p = event.getPackageName();
        return p != null ? p.toString() : null;
    }
    private boolean isSystemUiOrSelf(String pkg) {
        if (pkg == null) return true;
        if (pkg.equals(getPackageName()) || pkg.equals("android")) return true;
        if (pkg.equals("com.android.systemui")) return true;
        return false;
    }
    private boolean isRealApp(String pkg) {
        if (pkg == null || pkg.isEmpty()) return false;
        if (isSystemUiOrSelf(pkg)) return false;
        if (pkg.equals(getLauncherPackage())) return true;
        try {
            return getPackageManager().getLaunchIntentForPackage(pkg) != null;
        } catch (Exception e) { return false; }
    }
    private void scheduleForegroundApply(String pkg) {
        pendingFgPackage = pkg;
        Log.d(TAG, "收到前台窗口事件: " + pkg);
        if (pendingApplyRunnable == null) {
            pendingApplyRunnable = new Runnable() {
                @Override public void run() {
                    String target = pendingFgPackage;
                    if (target == null || target.isEmpty()) return;
                    if (!isRealApp(target)) return;
                    Log.d(TAG, "防抖确认前台: " + target);
                    if (applyForPackage(target)) updatePersistentNotification(target);
                }
            };
        }
        if (fgHandler != null) {
            fgHandler.removeCallbacks(pendingApplyRunnable);
            fgHandler.postDelayed(pendingApplyRunnable, FG_DEBOUNCE_MS);
        } else {
            new Thread(() -> {
                try { Thread.sleep(FG_DEBOUNCE_MS); } catch (InterruptedException e) { return; }
                pendingApplyRunnable.run();
            }).start();
        }
    }
    @Override
    public void onInterrupt() {
        Log.d(TAG, "无障碍服务中断");
        checkAndRestartService();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "无障碍服务销毁");
        try { if (servicePrefs != null && prefListener != null) servicePrefs.unregisterOnSharedPreferenceChangeListener(prefListener); } catch (Exception ignored) {}
        AutoOverclockManager.clearCustomOverride();
        lastAppliedConfig = "";
        checkAndRestartService();
    }
    private synchronized boolean applyForPackage(String basePkg) {
        if (basePkg == null || basePkg.isEmpty()) return false;
        if (isSystemUiOrSelf(basePkg)) return false;
        SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        if (!prefs.getBoolean("custom_app_refresh", false)) {
            AutoOverclockManager.clearCustomOverride();
            return false;
        }
        String authMode = prefs.getString("auth_mode", "");
        if (authMode == null || authMode.isEmpty()) return false;
        String effectivePkg = resolveEffectivePkg(prefs, basePkg);
        boolean enabled = prefs.getBoolean("app_refresh_enabled_" + effectivePkg, false);
        if (!enabled) {
            boolean backHome = basePkg.equals(getLauncherPackage());
            if (!backHome) {
                if (!isRealApp(basePkg)) return false;
                Log.d(TAG, "保持自定义应用 override，忽略: " + basePkg);
                return false;
            }
            Log.d(TAG, "回到桌面，交还自动守护");
            AutoOverclockManager.clearCustomOverride();
            lastAppliedConfig = "";
            stickyPkg = "";
            return false;
        }
        String res = prefs.getString("app_refresh_res_" + effectivePkg, "");
        int hz = prefs.getInt("app_refresh_hz_" + effectivePkg, -1);
        if (res == null || res.isEmpty() || hz <= 0) return false;
        String configKey = effectivePkg + "@" + res + "@" + hz;
        if (configKey.equals(lastAppliedConfig)) return false;
        currentFgPackage = basePkg;
        lastAppliedConfig = configKey;
        stickyPkg = effectivePkg;
        Log.d(TAG, "自定义刷新率切换: " + effectivePkg + " → " + res + " @ " + hz + "Hz");
        AutoOverclockManager.setCustomOverride(res, hz);
        applyDisplayTarget(authMode, res, hz);
        return true;
    }
    private void updatePersistentNotification(String basePkg) {
        try {
            SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
            if (!prefs.getBoolean("custom_app_refresh", false)) { cancelCustomNotification(); return; }
            if (basePkg == null || basePkg.isEmpty()) return;
            if (isIgnoredForNotification(prefs, basePkg)) return;
            lastRealPkg = basePkg;
            String effectivePkg = resolveEffectivePkg(prefs, basePkg);
            boolean enabled = prefs.getBoolean("app_refresh_enabled_" + effectivePkg, false);
            if (!enabled) return;
            String setRes = prefs.getString("app_refresh_res_" + effectivePkg, "");
            int setHz = prefs.getInt("app_refresh_hz_" + effectivePkg, -1);
            postCustomToast(basePkg, setRes, setHz);
        } catch (Exception e) {
            Log.e(TAG, "updatePersistentNotification: " + e.getMessage());
        }
    }
    private boolean isIgnoredForNotification(SharedPreferences prefs, String pkg) {
        if (pkg.equals("android") || pkg.equals(getPackageName())) return true;
        if (pkg.equals("com.android.systemui")) return true;
        if (prefs.getBoolean("app_refresh_enabled_" + pkg, false)) return false;
        try {
            if (pkg.equals(getLauncherPackage())) return true;
            if (getPackageManager().getLaunchIntentForPackage(pkg) == null) return true;
        } catch (Exception ignored) {}
        return false;
    }
    private String getLauncherPackage() {
        try {
            Intent home = new Intent(Intent.ACTION_MAIN);
            home.addCategory(Intent.CATEGORY_HOME);
            android.content.pm.ResolveInfo ri = getPackageManager()
                    .resolveActivity(home, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY);
            if (ri != null && ri.activityInfo != null) return ri.activityInfo.packageName;
        } catch (Exception ignored) {}
        return "";
    }
    private void postCustomToast(String pkg, String setRes, int setHz) {
        try {
            if (!getSharedPreferences("s", MODE_PRIVATE).getBoolean("switch_toast_enabled", true)) return;
            Context lc = getLocalizedCtx();
            String appLabel = pkg;
            try {
                android.content.pm.PackageManager pm = getPackageManager();
                appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
            } catch (Exception ignored) {}
            String msg = lc.getString(R.string.custom_notif_toast_format, appLabel, setRes, setHz);
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Log.e(TAG, "postCustomToast: " + e.getMessage());
        }
    }
    private Context getLocalizedCtx() {
        try {
            SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
            String lang = prefs.getString("language", "zh");
            java.util.Locale locale = "zh-rTW".equals(lang) ? new java.util.Locale("zh", "TW") : new java.util.Locale(lang);
            android.content.res.Configuration config = new android.content.res.Configuration(getResources().getConfiguration());
            config.setLocale(locale);
            return createConfigurationContext(config);
        } catch (Exception e) { return this; }
    }
    private void cancelCustomNotification() {
        try {
            lastAppliedConfig = "";
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) nm.cancel(CUSTOM_NOTIF_ID);
        } catch (Exception ignored) {}
    }
    private String resolveEffectivePkg(SharedPreferences prefs, String basePkg) {
        try {
            android.app.ActivityManager am = (android.app.ActivityManager) getSystemService(ACTIVITY_SERVICE);
            List<android.app.ActivityManager.RunningAppProcessInfo> procs = am.getRunningAppProcesses();
            if (procs != null) {
                for (android.app.ActivityManager.RunningAppProcessInfo proc : procs) {
                    if (proc.importance != android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) continue;
                    if (proc.pkgList == null) continue;
                    boolean matches = false;
                    for (String p : proc.pkgList) {
                        if (p.equals(basePkg)) { matches = true; break; }
                    }
                    if (!matches) continue;
                    int userId = proc.uid / 100000;
                    if (userId > 0) {
                        String cloneKey = basePkg + ":u" + userId;
                        if (prefs.getBoolean("app_refresh_enabled_" + cloneKey, false)) {
                            return cloneKey;
                        }
                    }
                    return basePkg;
                }
            }
        } catch (Exception ignored) {}
        return basePkg;
    }
    private void applyDisplayTarget(String authMode, String res, int hz) {
        try {
            String[] wh = res.replace("×", "x").split("x");
            if (wh.length != 2) return;
            int targetW = Integer.parseInt(wh[0]);
            int targetH = Integer.parseInt(wh[1]);
            List<DisplayMode> modes = AutoOverclockManager.getSupportedModes(this);
            DisplayMode target = null;
            for (DisplayMode m : modes) {
                if (m.getWidth() == targetW && m.getHeight() == targetH && m.getRateInt() == hz) {
                    target = m;
                    break;
                }
            }
            if (target == null) {
                for (DisplayMode m : modes) {
                    if (m.getWidth() == targetW && m.getHeight() == targetH) {
                        if (target == null || Math.abs(m.getRateInt() - hz) < Math.abs(target.getRateInt() - hz)) {
                            target = m;
                        }
                    }
                }
            }
            if (target == null) return;
            if ("root".equals(authMode)) {
                RootUtils.setDisplayMode(target.getWidth(), target.getHeight(), target.getRateInt(), target.getSfIndex());
            } else if ("shizuku".equals(authMode)) {
                ShizukuUtils.setDisplayMode(target.getWidth(), target.getHeight(), target.getRateInt(), target.getSfIndex());
            }
            Log.d(TAG, "应用刷新率已切换: " + res + " @ " + hz + "Hz");
        } catch (Exception e) {
            Log.e(TAG, "应用刷新率切换失败: " + e.getMessage());
        }
    }
    private void checkAndRestartService() {
        SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        if (!prefs.getBoolean("auto_overclock", false)) return;
        String authMode = prefs.getString("auth_mode", "");
        String ocRes    = prefs.getString("oc_target_res", "");
        int    ocHz     = prefs.getInt("oc_target_hz", -1);
        if (authMode.isEmpty() || ocRes.isEmpty() || ocHz < 0) return;
        String[] wh = ocRes.replace("×", "x").split("x");
        if (wh.length < 2) return;
        try {
            int tw = Integer.parseInt(wh[0]);
            int th = Integer.parseInt(wh[1]);
            Intent si = new Intent(this, OverclockService.class);
            si.putExtra("auth_mode", authMode);
            si.putExtra("targetW", tw);
            si.putExtra("targetH", th);
            si.putExtra("targetHz", ocHz);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                startForegroundService(si);
            else
                startService(si);
        } catch (Exception e) {
            Log.e(TAG, "重启服务失败: " + e.getMessage());
        }
    }
}
