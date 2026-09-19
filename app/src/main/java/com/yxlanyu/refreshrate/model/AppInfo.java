package com.yxlanyu.refreshrate.model;
import android.graphics.drawable.Drawable;
public class AppInfo {
    private final String name;
    private final String packageName;
    private final Drawable icon;
    private final boolean systemApp;
    private final int userId;
    public AppInfo(String name, String packageName, Drawable icon, boolean systemApp) {
        this(name, packageName, icon, systemApp, 0);
    }
    public AppInfo(String name, String packageName, Drawable icon, boolean systemApp, int userId) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.systemApp = systemApp;
        this.userId = userId;
    }
    public String getName() { return name; }
    public String getPackageName() { return packageName; }
    public Drawable getIcon() { return icon; }
    public boolean isSystemApp() { return systemApp; }
    public int getUserId() { return userId; }
    public String getEffectivePkg() {
        return userId > 0 ? packageName + ":u" + userId : packageName;
    }
}
