package com.yxlanyu.refreshrate.util;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import java.util.Locale;
public class LanguageUtils {
    public static final String LANG_ZH = "zh";
    public static final String LANG_ZH_TW = "zh-rTW";
    public static final String LANG_EN = "en";
    public static final String LANG_JA = "ja";
    public static final String LANG_RU = "ru";
    public static final String LANG_KO = "ko";
    public static final String LANG_SYSTEM = "system";
    private static final String PREFS_NAME = "s";
    private static final String KEY_LANG = "language";
    public static void applyLanguage(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lang = prefs.getString(KEY_LANG, LANG_SYSTEM);
        setLocale(context, lang);
    }
    public static void setLanguageAndRecreate(Activity activity, String lang) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_LANG, lang).apply();
        setLocale(activity, lang);
        activity.recreate();
    }
    public static String getCurrentLang(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_LANG, LANG_SYSTEM);
    }
    private static Locale resolve(String lang) {
        if (LANG_SYSTEM.equals(lang)) {
            Configuration sys = Resources.getSystem().getConfiguration();
            return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                    ? sys.getLocales().get(0) : sys.locale;
        } else if (LANG_ZH_TW.equals(lang)) {
            return new Locale("zh", "TW");
        }
        return new Locale(lang);
    }
    public static Context wrap(Context context) {
        String lang = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_LANG, LANG_SYSTEM);
        Locale locale = resolve(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration(context.getResources().getConfiguration());
        config.setLocale(locale);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return context.createConfigurationContext(config);
        }
        context.getResources().updateConfiguration(config, context.getResources().getDisplayMetrics());
        return context;
    }
    private static void setLocale(Context context, String lang) {
        Locale locale = resolve(lang);
        Locale.setDefault(locale);
        Resources res = context.getResources();
        Configuration config = res.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLocale(locale);
        } else {
            config.locale = locale;
        }
        res.updateConfiguration(config, res.getDisplayMetrics());
    }
}
