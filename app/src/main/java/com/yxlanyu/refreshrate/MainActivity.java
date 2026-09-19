package com.yxlanyu.refreshrate;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.yxlanyu.refreshrate.fragment.HomeFragment;
import com.yxlanyu.refreshrate.fragment.CustomAppRefreshFragment;
import com.yxlanyu.refreshrate.fragment.SettingsFragment;
import com.yxlanyu.refreshrate.fragment.RemoteRefreshFragment;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.io.InputStream;
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private ImageView wallpaperView;
    private ImageView wallpaperView2;
    private boolean wp2Front = false;
    private BottomNavigationView bottomNav;
    private android.widget.LinearLayout customNav;
    private final int[] NAV_IDS = { R.id.nav_home, R.id.nav_custom_app_refresh, R.id.nav_remote, R.id.nav_settings };
    private final int[] NAV_ICONS = { R.drawable.ic_nav_home, R.drawable.ic_nav_scheme, R.drawable.ic_nav_remote, R.drawable.ic_nav_settings };
    private final int[] NAV_LABELS = { R.string.nav_home, R.string.nav_custom_app_refresh, R.string.nav_remote, R.string.nav_settings };
    private SharedPreferences prefs;
    private int currentNavId = -1;
    private boolean navHidden = false;
    private String appliedLang;
    @Override protected void onResume() {
        super.onResume();
        if (appliedLang != null && !appliedLang.equals(LanguageUtils.getCurrentLang(this))) {
            recreate();
            return;
        }
        if (wallpaperView != null && prefs != null) setWallpaperImage(false);
    }
    @Override protected void onCreate(Bundle s) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        appliedLang = LanguageUtils.getCurrentLang(this);
        createNotificationChannel();
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().setStatusBarColor(android.graphics.Color.TRANSPARENT);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        wallpaperView = findViewById(R.id.global_wallpaper);
        wallpaperView2 = findViewById(R.id.global_wallpaper2);
        bottomNav = findViewById(R.id.bottom_navigation);
        customNav = findViewById(R.id.custom_nav);
        buildCustomNav();
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        getSupportFragmentManager().registerFragmentLifecycleCallbacks(new androidx.fragment.app.FragmentManager.FragmentLifecycleCallbacks() {
            @Override public void onFragmentViewCreated(androidx.fragment.app.FragmentManager fm, androidx.fragment.app.Fragment f, android.view.View v, android.os.Bundle sb) {
                android.view.View title = v.findViewWithTag("page_title");
                if (title != null) title.setVisibility(prefs.getInt("nav_scheme", 0) == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
            }
        }, false);
        applyWallpaperAndUI();
        if (s == null) {
            currentNavId = R.id.nav_home;
            loadFragment(new HomeFragment(), false);
        }
        applyNavScheme();
        bottomNav.setOnItemSelectedListener(item -> switchToNav(item.getItemId()));
        bottomNav.setOnItemReselectedListener(item -> {});
    }
    private boolean switchToNav(int id) {
        if (id == currentNavId) return true;
        showBottomNav();
        int oldNavId = currentNavId;
        Fragment fragment;
        if (id == R.id.nav_home) {
            fragment = new HomeFragment();
        } else if (id == R.id.nav_custom_app_refresh) {
            fragment = new CustomAppRefreshFragment();
        } else if (id == R.id.nav_remote) {
            fragment = new RemoteRefreshFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new SettingsFragment();
        } else {
            return false;
        }
        currentNavId = id;
        if (bottomNav != null && bottomNav.getMenu().findItem(id) != null) bottomNav.getMenu().findItem(id).setChecked(true);
        loadFragment(fragment, getNavIndex(id) > getNavIndex(oldNavId));
        setWallpaperImage(true);
        updateCustomNavSelection();
        return true;
    }
    private void buildCustomNav() {
        if (customNav == null) return;
        customNav.removeAllViews();
        for (int i = 0; i < NAV_IDS.length; i++) {
            final int navId = NAV_IDS[i];
            android.widget.LinearLayout item = new android.widget.LinearLayout(this);
            item.setOrientation(android.widget.LinearLayout.VERTICAL);
            item.setGravity(android.view.Gravity.CENTER);
            item.setClickable(true);
            item.setFocusable(true);
            item.setPadding(dpToPx(4), dpToPx(6), dpToPx(4), dpToPx(6));
            item.setTag(navId);
            android.util.TypedValue tvv = new android.util.TypedValue();
            getTheme().resolveAttribute(android.R.attr.selectableItemBackgroundBorderless, tvv, true);
            item.setBackgroundResource(tvv.resourceId);
            android.widget.ImageView icon = new android.widget.ImageView(this);
            icon.setImageResource(NAV_ICONS[i]);
            icon.setLayoutParams(new android.widget.LinearLayout.LayoutParams(dpToPx(26), dpToPx(26)));
            android.widget.TextView label = new android.widget.TextView(this);
            label.setText(getString(NAV_LABELS[i]));
            label.setTextSize(11);
            label.setMaxLines(1);
            label.setPadding(0, dpToPx(2), 0, 0);
            label.setGravity(android.view.Gravity.CENTER);
            item.addView(icon);
            item.addView(label);
            item.setOnClickListener(v -> switchToNav(navId));
            customNav.addView(item);
        }
        updateCustomNavSelection();
    }
    private void updateCustomNavSelection() {
        if (customNav == null) return;
        for (int i = 0; i < customNav.getChildCount(); i++) {
            android.view.View item = customNav.getChildAt(i);
            Object tag = item.getTag();
            boolean sel = tag != null && ((Integer) tag) == currentNavId;
            int color = sel ? 0xFF1976D2 : 0xFF9AA0A6;
            if (item instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) item;
                for (int j = 0; j < vg.getChildCount(); j++) {
                    android.view.View c = vg.getChildAt(j);
                    if (c instanceof android.widget.ImageView) ((android.widget.ImageView) c).setColorFilter(color);
                    else if (c instanceof android.widget.TextView) ((android.widget.TextView) c).setTextColor(color);
                }
            }
        }
    }
    private void applyCustomNavScheme(int scheme, android.view.View container) {
        if (customNav == null) return;
        boolean horizontal = (scheme == 2);
        customNav.setOrientation(horizontal ? android.widget.LinearLayout.HORIZONTAL : android.widget.LinearLayout.VERTICAL);
        // translucent rounded floating pill
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(android.graphics.Color.parseColor("#A6FFFFFF"));
        bg.setCornerRadius(dpToPx(28));
        bg.setStroke(dpToPx(1), android.graphics.Color.parseColor("#80FFFFFF"));
        customNav.setBackground(bg);
        customNav.setElevation(dpToPx(10));
        int pad = dpToPx(6);
        customNav.setPadding(pad, pad, pad, pad);
        int labelSp = horizontal ? 15 : 13;
        for (int i = 0; i < customNav.getChildCount(); i++) {
            android.view.View item = customNav.getChildAt(i);
            android.widget.LinearLayout.LayoutParams lp;
            if (horizontal) {
                lp = new android.widget.LinearLayout.LayoutParams(0, android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
            } else {
                lp = new android.widget.LinearLayout.LayoutParams(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
                lp.topMargin = dpToPx(4); lp.bottomMargin = dpToPx(4);
            }
            item.setLayoutParams(lp);
            if (item instanceof android.view.ViewGroup) {
                android.view.ViewGroup vg = (android.view.ViewGroup) item;
                for (int j = 0; j < vg.getChildCount(); j++) {
                    android.view.View c = vg.getChildAt(j);
                    if (c instanceof android.widget.ImageView) c.setVisibility(scheme == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
                    else if (c instanceof android.widget.TextView) ((android.widget.TextView) c).setTextSize(labelSp);
                }
            }
        }
        android.view.ViewGroup.LayoutParams glp = customNav.getLayoutParams();
        if (glp instanceof androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) {
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams clp = (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) glp;
            int P = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
            int U = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.UNSET;
            int W = android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
            clp.topToTop = U; clp.bottomToBottom = U; clp.startToStart = U; clp.endToEnd = U;
            clp.leftMargin = 0; clp.rightMargin = 0; clp.topMargin = 0; clp.bottomMargin = 0;
            int m = dpToPx(12);
            if (scheme == 2) { // top floating bar
                clp.topToTop = P; clp.startToStart = P; clp.endToEnd = P;
                clp.width = 0; clp.height = W;
                clp.leftMargin = dpToPx(16); clp.rightMargin = dpToPx(16); clp.topMargin = dpToPx(10);
            } else if (scheme == 3) { // left floating, centered vertically
                clp.startToStart = P; clp.topToTop = P; clp.bottomToBottom = P;
                clp.width = dpToPx(66); clp.height = W;
                clp.leftMargin = m;
            } else { // right floating, centered vertically
                clp.endToEnd = P; clp.topToTop = P; clp.bottomToBottom = P;
                clp.width = dpToPx(66); clp.height = W;
                clp.rightMargin = m;
            }
            customNav.setLayoutParams(clp);
        }
        // side schemes push content clear of the nav so it stays tappable; top pushes content down
        if (container != null) {
            int lp = (scheme == 3) ? dpToPx(88) : 0;
            int rp = (scheme == 4) ? dpToPx(88) : 0;
            int tp = (scheme == 2) ? dpToPx(66) : 0;
            container.setPadding(lp, tp, rp, 0);
        }
        updateCustomNavSelection();
    }
    private void loadFragment(Fragment fragment, boolean forward) {
        android.content.SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragment_container, fragment).commit();
        if (sp.getBoolean("anim_engine", true)) {
            final String type = sp.getString("anim_type", "spring");
            final float speed = sp.getFloat("anim_speed", 1.0f);
            final boolean fwd = forward;
            final android.view.View container = findViewById(R.id.fragment_container);
            if (container != null) container.post(() -> animatePage(container, type, speed, fwd));
        }
    }
    private void animatePage(android.view.View view, String type, float speed, boolean forward) {
        if (view == null) return;
        long dur = (long) (320 / (speed <= 0 ? 1f : speed));
        int w = view.getWidth();
        if (w <= 0) w = 400;
        view.animate().cancel();
        view.setAlpha(1f); view.setTranslationX(0f);
        if ("fade".equals(type)) {
            view.setAlpha(0f);
            view.animate().alpha(1f).setDuration(dur).setInterpolator(new android.view.animation.AccelerateDecelerateInterpolator()).start();
        } else if ("linear".equals(type)) {
            view.setTranslationX(forward ? w : -w);
            view.animate().translationX(0f).setDuration(dur).setInterpolator(new android.view.animation.LinearInterpolator()).start();
        } else if ("slide".equals(type)) {
            view.setAlpha(0f); view.setTranslationX(forward ? w * 0.35f : -w * 0.35f);
            view.animate().translationX(0f).alpha(1f).setDuration(dur).setInterpolator(new android.view.animation.DecelerateInterpolator()).start();
        } else {
            view.setTranslationX(forward ? w * 0.5f : -w * 0.5f);
            view.animate().translationX(0f).setDuration((long) (dur * 1.4f)).setInterpolator(new android.view.animation.OvershootInterpolator(1.6f)).start();
        }
    }
    private int getNavIndex(int id) {
        if (id == R.id.nav_home) return 0;
        if (id == R.id.nav_custom_app_refresh) return 1;
        if (id == R.id.nav_remote) return 2;
        if (id == R.id.nav_settings) return 3;
        return 0;
    }
    private String pageKeyForNav(int id) {
        if (id == R.id.nav_custom_app_refresh) return "app";
        if (id == R.id.nav_remote) return "remote";
        if (id == R.id.nav_settings) return "settings";
        return "home";
    }
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "overclock_channel",
                    getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    private void applyWallpaperAndUI() {
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float titleAlpha = 1f - wallpaperAlpha;
        bottomNav.getBackground().setAlpha((int)(titleAlpha * 255));
        bottomNav.setElevation(0);
        setWallpaperImage(false);
    }
    private android.graphics.drawable.Drawable currentWallpaperDrawable() {
        int type = prefs.getInt("wallpaper_type", 4);
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, pageKeyForNav(currentNavId));
            if (bm != null) return new android.graphics.drawable.BitmapDrawable(getResources(), bm);
            return androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white);
        }
        return WallpaperStore.presetDrawable(this, type);
    }
    private void setWallpaperImage(boolean animate) {
        android.graphics.drawable.Drawable newD = currentWallpaperDrawable();
        if (newD == null) return;
        float a = prefs.getFloat("wallpaper_alpha", 0.3f);
        ImageView front = wp2Front ? wallpaperView2 : wallpaperView;
        ImageView back = wp2Front ? wallpaperView : wallpaperView2;
        if (back == null || front == null) { wallpaperView.setImageDrawable(newD); wallpaperView.setAlpha(a); return; }
        if (!animate) {
            front.setImageDrawable(newD);
            front.setAlpha(a);
            back.setAlpha(0f);
            return;
        }
        back.animate().cancel();
        front.animate().cancel();
        back.setImageDrawable(newD);
        back.setAlpha(0f);
        back.animate().alpha(a).setDuration(300).start();
        front.animate().alpha(0f).setDuration(300).start();
        wp2Front = !wp2Front;
    }
    private void applyNavScheme() {
        int scheme = prefs.getInt("nav_scheme", 0);
        android.view.View container = findViewById(R.id.fragment_container);
        if (scheme == 0 || scheme == 1) {
            if (customNav != null) customNav.setVisibility(android.view.View.GONE);
            if (bottomNav != null) bottomNav.setVisibility(android.view.View.VISIBLE);
            if (container != null) container.setPadding(0, 0, 0, 0);
            if (scheme == 0) applyFloatingNav(); else applyClassicNav();
        } else {
            if (bottomNav != null) bottomNav.setVisibility(android.view.View.GONE);
            if (customNav != null) customNav.setVisibility(android.view.View.VISIBLE);
            applyCustomNavScheme(scheme, container);
        }
        updatePageTitleVisibility();
    }
    private void updatePageTitleVisibility() {
        android.view.View root = findViewById(R.id.fragment_container);
        if (root == null) return;
        android.view.View title = root.findViewWithTag("page_title");
        if (title != null) title.setVisibility(prefs.getInt("nav_scheme", 0) == 2 ? android.view.View.GONE : android.view.View.VISIBLE);
    }
    private void applyFloatingNav() {
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
        params.leftMargin = dpToPx(32);
        params.rightMargin = dpToPx(32);
        params.bottomMargin = dpToPx(24);
        bottomNav.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bottomNav.setRenderEffect(null);
        }
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(40));
        bg.setColor(android.graphics.Color.parseColor("#D8FFFFFF"));
        bg.setStroke(dpToPx(1), android.graphics.Color.parseColor("#80FFFFFF"));
        bottomNav.setBackground(bg);
        bottomNav.setElevation(dpToPx(10));
    }
    private void applyClassicNav() {
        android.view.ViewGroup.MarginLayoutParams params = (android.view.ViewGroup.MarginLayoutParams) bottomNav.getLayoutParams();
        params.leftMargin = 0;
        params.rightMargin = 0;
        params.bottomMargin = 0;
        bottomNav.setLayoutParams(params);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            bottomNav.setRenderEffect(null);
        }
        bottomNav.setBackgroundColor(android.graphics.Color.parseColor("#B0FFFFFF"));
        bottomNav.setElevation(0);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key == null) return;
        if (key.startsWith("wallpaper_")) {
            applyWallpaperAndUI();
        }
        if ("nav_scheme".equals(key)) {
            applyNavScheme();
        }
    }
    public void hideBottomNav() {
        if (navHidden) return;
        navHidden = true;
        if (prefs.getInt("nav_scheme", 0) <= 1 && bottomNav != null) bottomNav.animate().translationY(bottomNav.getHeight() + dpToPx(20)).setDuration(220).start();
    }
    public void showBottomNav() {
        if (!navHidden) return;
        navHidden = false;
        if (prefs.getInt("nav_scheme", 0) <= 1 && bottomNav != null) bottomNav.animate().translationY(0).setDuration(220).start();
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
    }
}
