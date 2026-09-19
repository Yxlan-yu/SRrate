package com.yxlanyu.refreshrate.util;
import android.content.ComponentName;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import com.yxlanyu.refreshrate.service.KeepAliveAccessibilityService;
import java.util.Locale;
public class AccessibilityUtils {
    public static boolean isKeepAliveServiceEnabled(Context context) {
        if (context == null) return false;
        try {
            String enabledServices = Settings.Secure.getString(
                    context.getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (TextUtils.isEmpty(enabledServices)) return false;
            ComponentName target = new ComponentName(context, KeepAliveAccessibilityService.class);
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(':');
            splitter.setString(enabledServices);
            while (splitter.hasNext()) {
                String item = splitter.next();
                if (TextUtils.isEmpty(item)) continue;
                ComponentName enabled = ComponentName.unflattenFromString(item);
                if (target.equals(enabled)) return true;
                String normalized = item.trim().toLowerCase(Locale.ROOT);
                String pkg = context.getPackageName().toLowerCase(Locale.ROOT);
                String full = (context.getPackageName() + "/" + KeepAliveAccessibilityService.class.getName()).toLowerCase(Locale.ROOT);
                String shortName = (context.getPackageName() + "/.service.KeepAliveAccessibilityService").toLowerCase(Locale.ROOT);
                if (normalized.equals(full) || normalized.equals(shortName)) return true;
                if (normalized.startsWith(pkg + "/") && normalized.endsWith(".keepaliveaccessibilityservice")) return true;
            }
        } catch (Exception ignored) {}
        return false;
    }
}
