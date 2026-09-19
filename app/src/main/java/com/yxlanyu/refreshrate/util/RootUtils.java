package com.yxlanyu.refreshrate.util;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.yxlanyu.refreshrate.model.DisplayMode;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RootUtils {
    private static final String TAG = "RootUtils";
    public static boolean isRooted() {
        Process p = null;
        DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("echo RootOK\nexit\n"); os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            return "RootOK".equals(r.readLine());
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close();
            } catch(Exception ignored){}
            try { if(r != null) r.close();
            } catch(Exception ignored){}
            try { if(p != null) p.destroy();
            } catch(Exception ignored){}
        }
    }
    public static List<DisplayMode> getDisplayModesFromDumpsys() {
        List<DisplayMode> list = new ArrayList<>();
        Process p = null; DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep 'DisplayModeRecord'\nexit\n");
            os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            Pattern pattern = Pattern.compile("id=(\\d+),\\s*width=(\\d+),\\s*height=(\\d+),\\s*fps=([\\d.]+)");
            int sfIdx = 0;
            while ((line = r.readLine()) != null) {
                Matcher m = pattern.matcher(line);
                if (m.find()) {
                    int id = Integer.parseInt(m.group(1));
                    int w = Integer.parseInt(m.group(2));
                    int h = Integer.parseInt(m.group(3));
                    float fps = Float.parseFloat(m.group(4));
                    DisplayMode dmode = new DisplayMode(w, h, fps, id);
                    dmode.setSfIndex(sfIdx++);
                    list.add(dmode);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "解析dumpsys失败", e);
        } finally {
            try { if(os != null) os.close();
            } catch(Exception ignored){}
            try { if(r != null) r.close();
            } catch(Exception ignored){}
            try { if(p != null) p.destroy();
            } catch(Exception ignored){}
        }
        return list;
    }
    public static String buildSwitchScript(int w, int h, int hz, int sfIndex, java.util.List<Integer> ladder) {
        StringBuilder s = new StringBuilder();
        if (w > 0 && h > 0 && hz > 0)
            s.append("cmd display set-user-preferred-display-mode ").append(w).append(" ").append(h).append(" ").append(hz).append(" 2>/dev/null; ");
        s.append("settings put system peak_refresh_rate ").append(hz).append(".0; ");
        s.append("settings put system min_refresh_rate ").append(hz).append(".0; ");
        s.append("settings put system user_refresh_rate ").append(hz).append("; ");
        s.append("settings put secure miui_refresh_rate ").append(hz).append("; ");
        if (sfIndex >= 0)
            s.append("service call SurfaceFlinger 1035 i32 ").append(sfIndex).append(" >/dev/null 2>&1");
        return s.toString();
    }
    private static final java.util.concurrent.atomic.AtomicInteger SWITCH_SEQ = new java.util.concurrent.atomic.AtomicInteger(0);
    public static boolean setDisplayMode(int w, int h, int targetHz, int sfIndex) {
        return doSetDisplayMode(w, h, targetHz, sfIndex, SWITCH_SEQ.incrementAndGet());
    }
    private static synchronized boolean doSetDisplayMode(int w, int h, int targetHz, int sfIndex, int mySeq) {
        if (mySeq != SWITCH_SEQ.get()) return true;
        runSu(buildSwitchScript(w, h, targetHz, sfIndex, null));
        return true;
    }
    private static void runSu(String cmd) {
        Process p = null;
        DataOutputStream os = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes(cmd + "\nexit\n"); os.flush();
            p.waitFor();
        } catch (Exception ignored) {
        } finally {
            try { if (os != null) os.close(); } catch (Exception ig) {}
            try { if (p != null) p.destroy(); } catch (Exception ig) {}
        }
    }
    private static int activeModeId() {
        Process p = null;
        DataOutputStream os = null;
        BufferedReader br = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep -m1 mActiveModeId\nexit\n"); os.flush();
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            Pattern pat = Pattern.compile("mActiveModeId=(\\d+)");
            while ((line = br.readLine()) != null) {
                Matcher mm = pat.matcher(line);
                if (mm.find()) return Integer.parseInt(mm.group(1));
            }
        } catch (Exception ignored) {
        } finally {
            try { if (br != null) br.close(); } catch (Exception ig) {}
            try { if (os != null) os.close(); } catch (Exception ig) {}
            try { if (p != null) p.destroy(); } catch (Exception ig) {}
        }
        return -1;
    }
    public static boolean restoreAdaptive(int minHz, int maxHz) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("cmd display clear-user-preferred-display-mode 2>/dev/null\n");
            if (minHz > 0) os.writeBytes("settings put system min_refresh_rate " + minHz + ".0\n");
            if (maxHz > 0) os.writeBytes("settings put system peak_refresh_rate " + maxHz + ".0\n");
            os.writeBytes("exit\n"); os.flush();
            process.waitFor();
            return true;
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close(); } catch(Exception ignored){}
            try { if(process != null) process.destroy(); } catch(Exception ignored){}
        }
    }
    public static boolean setNativeRefreshOverlay(boolean on) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("service call SurfaceFlinger 1034 i32 " + (on ? 1 : 0) + "\n");
            os.writeBytes("exit\n"); os.flush();
            process.waitFor();
            return true;
        } catch (Exception e) { return false;
        } finally {
            try { if(os != null) os.close(); } catch(Exception ignored){}
            try { if(process != null) process.destroy(); } catch(Exception ignored){}
        }
    }
    public static String generateRuntimeLog() { return generateRuntimeLog(null); }
    public static String generateRuntimeLog(Context ctx) {
    StringBuilder sb = new StringBuilder();
    String ts = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(new java.util.Date());
    sb.append("===== 屏幕刷新率工具 运行调试日志 =====\n");
    sb.append("生成时间: ").append(ts).append("\n");
    sb.append("App版本: 1.6.5 (1650)\n\n");
    sb.append("【设备信息】\n");
    sb.append("品牌: ").append(android.os.Build.BRAND).append("\n");
    sb.append("型号: ").append(android.os.Build.MODEL).append("\n");
    sb.append("Android: ").append(android.os.Build.VERSION.RELEASE)
      .append("  (API ").append(android.os.Build.VERSION.SDK_INT).append(")\n");
    sb.append("ROM: ").append(android.os.Build.DISPLAY).append("\n\n");
    sb.append("【授权状态】\n");
    boolean rooted = isRooted();
    sb.append("Root: ").append(rooted ? "✓ 已获取" : "Ⅹ 未获取").append("\n");
    try {
        boolean shAvail = ShizukuUtils.isAvailable();
        boolean shPerm  = shAvail && ShizukuUtils.hasPermission();
        sb.append("Shizuku 服务: ").append(shAvail ? "✓ 运行中" : "Ⅹ 未运行/未安装").append("\n");
        sb.append("Shizuku 授权: ").append(shPerm ? "✓ 已授权" : "Ⅹ 未授权").append("\n");
    } catch (Throwable t) {
        sb.append("Shizuku: 检测异常 ").append(t.getMessage()).append("\n");
    }
    if (ctx != null) {
        SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
        String authMode = prefs.getString("auth_mode", "");
        sb.append("当前授权模式: ").append(authMode.isEmpty() ? "未选择" : authMode).append("\n");
        boolean a11y = AccessibilityUtils.isKeepAliveServiceEnabled(ctx);
        sb.append("无障碍服务: ").append(a11y ? "✓ 已开启" : "Ⅹ 未开启").append("\n");
        boolean notifOn = androidx.core.app.NotificationManagerCompat.from(ctx).areNotificationsEnabled();
        sb.append("通知权限: ").append(notifOn ? "✓ 已允许" : "Ⅹ 已被关闭").append("\n");
        if (android.os.Build.VERSION.SDK_INT >= 33) {
            boolean postPerm = ctx.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            sb.append("POST_NOTIFICATIONS: ").append(postPerm ? "✓ 已授予" : "Ⅹ 未授予").append("\n");
        }
        try {
            android.os.PowerManager pm = (android.os.PowerManager) ctx.getSystemService(Context.POWER_SERVICE);
            boolean ignoreBatt = pm != null && pm.isIgnoringBatteryOptimizations(ctx.getPackageName());
            sb.append("忽略电池优化: ").append(ignoreBatt ? "✓ 是" : "Ⅹ 否(可能被杀后台)").append("\n");
        } catch (Exception ignored) {}
    } else {
        sb.append("（无障碍/通知状态需在设置页生成才可读取）\n");
    }
    sb.append("\n");
    if (ctx != null) {
        SharedPreferences prefs = ctx.getSharedPreferences("s", Context.MODE_PRIVATE);
        sb.append("【功能状态】\n");
        sb.append("自定义应用刷新率: ").append(prefs.getBoolean("custom_app_refresh", false) ? "开" : "关").append("\n");
        sb.append("自动超频: ").append(prefs.getBoolean("auto_overclock", false) ? "开" : "关")
          .append("  (运行中: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.isRunning() ? "是" : "否").append(")\n");
        sb.append("超频目标: ").append(prefs.getString("oc_target_res", "未设置"))
          .append(" @ ").append(prefs.getInt("oc_target_hz", -1)).append("Hz\n");
        sb.append("锁定刷新率: ").append(prefs.getBoolean("rate_lock_enabled", false)
                ? ("锁定中 @ " + prefs.getInt("rate_lock_hz", 0) + "Hz") : "未锁定").append("\n");
        sb.append("超频最近日志: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getLastLog()).append("\n\n");
        sb.append("【已配置的单应用刷新率】\n");
        int cfgCount = 0;
        Map<String, ?> all = prefs.getAll();
        for (String k : all.keySet()) {
            if (k.startsWith("app_refresh_enabled_") && Boolean.TRUE.equals(all.get(k))) {
                String pkg = k.substring("app_refresh_enabled_".length());
                String res = prefs.getString("app_refresh_res_" + pkg, "?");
                int hz = prefs.getInt("app_refresh_hz_" + pkg, -1);
                sb.append("  • ").append(pkg).append(" → ").append(res).append(" @ ").append(hz).append("Hz\n");
                cfgCount++;
            }
        }
        if (cfgCount == 0) sb.append("  （暂无）\n");
        sb.append("\n");
        sb.append("【当前显示状态】\n");
        sb.append("当前分辨率: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getCurrentResolution(ctx)).append("\n");
        sb.append("当前刷新率: ").append(Math.round(com.yxlanyu.refreshrate.util.AutoOverclockManager.getCurrentRate(ctx))).append("Hz\n");
        sb.append("系统支持档位数: ").append(com.yxlanyu.refreshrate.util.AutoOverclockManager.getSupportedModes(ctx).size()).append("\n");
        sb.append("【档位ID映射(系统id↔软件执行id)】\n");
        for (com.yxlanyu.refreshrate.model.DisplayMode dm : com.yxlanyu.refreshrate.util.AutoOverclockManager.getSupportedModes(ctx)) {
            sb.append("  ").append(dm.getWidth()).append("×").append(dm.getHeight())
              .append(" @ ").append(dm.getRateInt()).append("Hz  系统id=").append(dm.getModeId())
              .append("  软件执行id=").append(dm.getSfIndex()).append("\n");
        }
        sb.append("\n");
    }
    sb.append("【刷新率显示开关诊断】\n");
    try {
        Process p3 = Runtime.getRuntime().exec("su");
        java.io.DataOutputStream os4 = new java.io.DataOutputStream(p3.getOutputStream());
        os4.writeBytes(
            "echo '=global='; settings list global | grep -i refresh\n" +
            "echo '=system='; settings list system | grep -i refresh\n" +
            "echo '=secure='; settings list secure | grep -i refresh\n" +
            "echo '=props='; getprop | grep -i 'refresh\\|fps\\|overlay'\n" +
            "exit\n"
        );
        os4.flush();
        java.io.BufferedReader r3 = new java.io.BufferedReader(new java.io.InputStreamReader(p3.getInputStream()));
        String line3; while ((line3 = r3.readLine()) != null) sb.append(line3).append("\n");
        p3.waitFor();
    } catch (Exception e4) { sb.append("诊断失败(可能无Root): ").append(e4.getMessage()).append("\n"); }
    sb.append("\n");
    sb.append("【Raw dumpsys 档位列表】\n");
    if (!rooted) {
        sb.append("（需要Root权限才能读取）\n");
    } else {
        Process p = null;
        DataOutputStream os = null; BufferedReader r = null;
        try {
            p = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("dumpsys display | grep 'DisplayModeRecord'\nexit\n");
            os.flush();
            r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int count = 0;
            while ((line = r.readLine()) != null) {
                sb.append(line.trim()).append("\n");
                count++;
            }
            if (count == 0) sb.append("（无输出）\n");
        } catch (Exception e) {
            sb.append("提取错误: ").append(e.getMessage()).append("\n");
        } finally {
            try { if(os!=null)os.close();
            }catch(Exception ignored){}
            try { if(r!=null)r.close();
            }catch(Exception ignored){}
            try { if(p!=null)p.destroy();
            }catch(Exception ignored){}
        }
    }
    sb.append("\n=====\n");
    return sb.toString();
}
}
