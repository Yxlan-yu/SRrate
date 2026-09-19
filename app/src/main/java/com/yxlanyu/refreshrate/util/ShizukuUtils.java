package com.yxlanyu.refreshrate.util;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.*;
import rikka.shizuku.Shizuku;
public class ShizukuUtils {
    private static final String TAG = "ShizukuUtils";
    public static final int REQUEST_CODE = 1001;
    public static boolean isAvailable() {
        try { return Shizuku.pingBinder();
        }
        catch (Exception e) { return false;
        }
    }
    public static boolean hasPermission() {
        try {
            if (Shizuku.isPreV11()) return false;
            return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
        } catch (Exception e) { return false;
        }
    }
    public static void requestPermission() {
        try { Shizuku.requestPermission(REQUEST_CODE);
        }
        catch (Exception e) { Log.e(TAG, "requestPermission: " + e.getMessage());
        }
    }
    public static boolean setDisplayMode(int w, int h, int hz, int sfIndex) {
        if (!isAvailable() || !hasPermission()) return false;
        execShizuku(RootUtils.buildSwitchScript(w, h, hz, sfIndex, null));
        try {
            if (sfIndex >= 0 && hz > 0) {
                Thread.sleep(180);
                int landedSf = activeSfIndexShizuku();
                if (landedSf >= 0 && landedSf != sfIndex) {
                    int correct = 2 * sfIndex - landedSf;
                    if (correct >= 0 && correct < 64)
                        execShizuku("service call SurfaceFlinger 1035 i32 " + correct + " >/dev/null 2>&1; settings put system peak_refresh_rate " + hz + ".0; settings put system min_refresh_rate " + hz + ".0");
                }
            }
        } catch (Exception ignored) {}
        return true;
    }
    private static int activeSfIndexShizuku() {
        try {
            String out = execAndRead("dumpsys display");
            if (out == null) return -1;
            java.util.regex.Matcher a = java.util.regex.Pattern.compile("mActiveModeId=(\\d+)").matcher(out);
            if (!a.find()) return -1;
            int activeId = Integer.parseInt(a.group(1));
            java.util.regex.Matcher m = java.util.regex.Pattern.compile("id=(\\d+),\\s*width=").matcher(out);
            int sf = 0;
            while (m.find()) { if (Integer.parseInt(m.group(1)) == activeId) return sf; sf++; }
        } catch (Exception ignored) {}
        return -1;
    }
    public static boolean restoreAdaptive(int minHz, int maxHz) {
        if (!isAvailable() || !hasPermission()) return false;
        StringBuilder sb = new StringBuilder();
        sb.append("cmd display clear-user-preferred-display-mode 2>/dev/null; ");
        if (minHz > 0) sb.append("settings put system min_refresh_rate ").append(minHz).append(".0; ");
        if (maxHz > 0) sb.append("settings put system peak_refresh_rate ").append(maxHz).append(".0");
        return execShizuku(sb.toString());
    }
    public static boolean setNativeRefreshOverlay(boolean on) {
        if (!isAvailable() || !hasPermission()) return false;
        return execShizuku("service call SurfaceFlinger 1034 i32 " + (on ? 1 : 0));
    }
    public static String execAndRead(String cmd) {
        if (!isAvailable() || !hasPermission()) return "";
        try {
            Process p = execShizukuProcess(cmd);
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) sb.append(line).append("\n");
            p.waitFor();
            return sb.toString().trim();
        } catch (Exception e) {
            Log.e(TAG, "execAndRead: " + e.getMessage());
            return "";
        }
    }
    private static boolean execShizuku(String cmd) {
        try {
            Process p = execShizukuProcess(cmd);
            int code = p.waitFor();
            Log.d(TAG, "execShizuku exit=" + code + " cmd=" + cmd);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "execShizuku: " + e.getMessage());
            return false;
        }
    }
    private static Process execShizukuProcess(String cmd) throws Exception {
        java.lang.reflect.Method method = Shizuku.class.getDeclaredMethod(
            "newProcess", String[].class, String[].class, String.class);
        method.setAccessible(true);
        return (Process) method.invoke(null, new String[]{"sh", "-c", cmd}, null, null);
    }
}
