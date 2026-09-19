package com.yxlanyu.refreshrate;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
public class AppRefreshConfigActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private String pkg;
    private View targetContainer;
    private Spinner spinnerRes;
    private Spinner spinnerRate;
    private final List<Integer>[] rateHolder = new List[]{new ArrayList<>()};
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_app_refresh_config);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        pkg = getIntent().getStringExtra("pkg");
        if (pkg == null || pkg.trim().isEmpty()) {
            finish();
            return;
        }
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());
        targetContainer = findViewById(R.id.ll_app_target_container);
        spinnerRes = findViewById(R.id.spinner_app_resolution);
        spinnerRate = findViewById(R.id.spinner_app_rate);
        bindAppHeader();
        setupTargetSelectors();
        setupSwitch();
    }
    private void bindAppHeader() {
        ImageView icon = findViewById(R.id.iv_app_icon);
        TextView name = findViewById(R.id.tv_app_name);
        TextView pkgView = findViewById(R.id.tv_app_package);
        PackageManager pm = getPackageManager();
        try {
            ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
            icon.setImageDrawable(pm.getApplicationIcon(ai));
            name.setText(pm.getApplicationLabel(ai));
            pkgView.setText(pkg);
        } catch (Exception e) {
            name.setText(pkg);
            pkgView.setText(pkg);
        }
    }
    private void setupSwitch() {
        SwitchCompat sw = findViewById(R.id.switch_single_app_refresh);
        boolean enabled = prefs.getBoolean(key("enabled"), false);
        sw.setChecked(enabled);
        showTargetContainer(enabled);
        sw.setOnCheckedChangeListener((buttonView, checked) -> {
            if (checked) {
                if (!prefs.getBoolean("custom_app_refresh", false)) {
                    Toast.makeText(this, R.string.custom_app_master_required, Toast.LENGTH_SHORT).show();
                    sw.setChecked(false);
                    return;
                }
                if (!isAccessibilityServiceEnabled()) {
                    Toast.makeText(this, R.string.accessibility_required, Toast.LENGTH_LONG).show();
                    try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
                    catch (Exception e) { Toast.makeText(this, R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show(); }
                    sw.setChecked(false);
                    return;
                }
                if (prefs.getString("auth_mode", "").isEmpty()) {
                    Toast.makeText(this, R.string.no_any_permission, Toast.LENGTH_SHORT).show();
                    sw.setChecked(false);
                    return;
                }
            }
            prefs.edit().putBoolean(key("enabled"), checked).apply();
            showTargetContainer(checked);
            if (checked) saveCurrentSelections();
        });
    }
    private void setupTargetSelectors() {
        List<DisplayMode> allModes = AutoOverclockManager.getSupportedModes(this);
        LinkedHashMap<String, List<DisplayMode>> resMap = new LinkedHashMap<>();
        for (DisplayMode m : allModes) {
            String k = m.getWidth() + "x" + m.getHeight();
            resMap.computeIfAbsent(k, kk -> new ArrayList<>()).add(m);
        }
        List<String> resKeys = new ArrayList<>(resMap.keySet());
        resKeys.sort((a, b) -> {
            try {
                String[] pa = a.split("x"), pb = b.split("x");
                return Integer.parseInt(pb[0]) * Integer.parseInt(pb[1]) - Integer.parseInt(pa[0]) * Integer.parseInt(pa[1]);
            } catch (Exception e) {
                return a.compareTo(b);
            }
        });
        List<String> resLabels = new ArrayList<>();
        for (String k : resKeys) resLabels.add(k.replace("x", "×"));
        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, resLabels);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRes.setAdapter(resAdapter);
        String savedRes = prefs.getString(key("res"), "");
        int savedResIdx = resKeys.indexOf(savedRes);
        if (savedResIdx < 0) savedResIdx = 0;
        if (!resKeys.isEmpty()) {
            spinnerRes.setSelection(savedResIdx);
            if (savedRes.isEmpty()) prefs.edit().putString(key("res"), resKeys.get(0)).apply();
        }
        Runnable populateRates = () -> {
            int resIdx = spinnerRes.getSelectedItemPosition();
            if (resIdx < 0 || resIdx >= resKeys.size()) return;
            String rk = resKeys.get(resIdx);
            List<DisplayMode> modes = resMap.get(rk);
            if (modes == null) return;
            modes.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            List<String> rateLabels = new ArrayList<>();
            rateHolder[0] = new ArrayList<>();
            for (DisplayMode m : modes) {
                rateLabels.add(m.getRateInt() + " Hz");
                rateHolder[0].add(m.getRateInt());
            }
            ArrayAdapter<String> rateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, rateLabels);
            rateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRate.setAdapter(rateAdapter);
            int savedHz = prefs.getInt(key("hz"), -1);
            int idx = rateHolder[0].indexOf(savedHz);
            if (!rateHolder[0].isEmpty()) {
                spinnerRate.setSelection(idx >= 0 ? idx : 0);
                if (savedHz < 0) prefs.edit().putInt(key("hz"), rateHolder[0].get(0)).apply();
            }
        };
        populateRates.run();
        spinnerRes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= resKeys.size()) return;
                prefs.edit().putString(key("res"), resKeys.get(position)).apply();
                populateRates.run();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
        spinnerRate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> parent, android.view.View view, int position, long id) {
                if (position < 0 || position >= rateHolder[0].size()) return;
                prefs.edit().putInt(key("hz"), rateHolder[0].get(position)).apply();
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });
    }
    private void saveCurrentSelections() {
        if (spinnerRes == null || spinnerRate == null) return;
        int rIdx = spinnerRes.getSelectedItemPosition();
        int hIdx = spinnerRate.getSelectedItemPosition();
        String resVal = prefs.getString(key("res"), "");
        int    hzVal  = prefs.getInt(key("hz"), -1);
        if (resVal.isEmpty() && spinnerRes.getAdapter() != null && rIdx >= 0) {
            String label = (String) spinnerRes.getAdapter().getItem(rIdx);
            if (label != null) prefs.edit().putString(key("res"), label.replace("×", "x")).apply();
        }
        if (hzVal < 0 && spinnerRate.getAdapter() != null && hIdx >= 0) {
            String label = (String) spinnerRate.getAdapter().getItem(hIdx);
            if (label != null) {
                try { prefs.edit().putInt(key("hz"), Integer.parseInt(label.replace(" Hz", "").trim())).apply(); } catch (Exception ignored) {}
            }
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(this);
    }
    private String key(String suffix) {
        return "app_refresh_" + suffix + "_" + pkg;
    }
    private void showTargetContainer(boolean show) {
        if (targetContainer != null) targetContainer.setVisibility(show ? View.VISIBLE : View.GONE);
    }
    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
