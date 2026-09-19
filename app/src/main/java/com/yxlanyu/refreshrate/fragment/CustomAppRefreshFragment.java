package com.yxlanyu.refreshrate.fragment;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import com.yxlanyu.refreshrate.AppListActivity;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
public class CustomAppRefreshFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private SharedPreferences prefs;
    private View titleBar;
    private TextView titleText;
    private TextView tvOCStatus;
    private TextView tvOCCur;
    private View ocTargetContainer;
    private Handler overclockHandler;
    private Runnable overclockUpdater;
    private android.widget.LinearLayout llEnabledApps;
    private android.widget.TextView tvEnabledEmpty;
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_custom_app_refresh, c, false);
        titleBar = v.findViewById(R.id.custom_app_refresh_title_bar);
        titleText = v.findViewById(R.id.custom_app_refresh_title_text);
        tvOCStatus = v.findViewById(R.id.tv_overclock_status);
        tvOCCur = v.findViewById(R.id.tv_overclock_cur);
        ocTargetContainer = v.findViewById(R.id.ll_oc_target_container);
        llEnabledApps = v.findViewById(R.id.ll_enabled_apps);
        tvEnabledEmpty = v.findViewById(R.id.tv_enabled_empty);
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        v.findViewById(R.id.row_app_management).setOnClickListener(view -> {
            try {
                startActivity(new Intent(requireContext(), AppListActivity.class));
                requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            } catch (Exception e) {
                Toast.makeText(getContext(), R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show();
            }
        });
        setupSwitchesAndTargets(v);
        updateUI();
        updateCurrentDisplayText();
        return v;
    }
    private void setupSwitchesAndTargets(View v) {
        SwitchCompat switchCustom = v.findViewById(R.id.switch_custom_app_refresh);
        SwitchCompat switchOC = v.findViewById(R.id.switch_auto_overclock);
        Spinner spinnerRes = v.findViewById(R.id.spinner_oc_resolution);
        Spinner spinnerRate = v.findViewById(R.id.spinner_oc_rate);
        final boolean[] mute = {false};
        List<DisplayMode> allModes = AutoOverclockManager.getSupportedModes(requireContext());
        LinkedHashMap<String, List<DisplayMode>> resMap = new LinkedHashMap<>();
        for (DisplayMode m : allModes) {
            String key = m.getWidth() + "x" + m.getHeight();
            resMap.computeIfAbsent(key, k -> new ArrayList<>()).add(m);
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
        ArrayAdapter<String> resAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, resLabels);
        resAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRes.setAdapter(resAdapter);
        String savedResVal = prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0));
        int savedResIdx = resKeys.indexOf(savedResVal);
        if (savedResIdx < 0) savedResIdx = 0;
        if (!resKeys.isEmpty()) {
            spinnerRes.setSelection(savedResIdx);
            if (prefs.getString("oc_target_res", "").isEmpty()) {
                prefs.edit().putString("oc_target_res", resKeys.get(savedResIdx)).apply();
            }
        }
        final List<Integer>[] rateHolder = new List[]{new ArrayList<>()};
        Runnable populateRates = () -> {
            int resIdx = spinnerRes.getSelectedItemPosition();
            if (resIdx < 0 || resIdx >= resKeys.size()) return;
            String rk = resKeys.get(resIdx);
            List<DisplayMode> ms = resMap.get(rk);
            if (ms == null) return;
            ms.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            List<String> rateLabels = new ArrayList<>();
            rateHolder[0] = new ArrayList<>();
            for (DisplayMode m : ms) {
                rateLabels.add(m.getRateInt() + " Hz");
                rateHolder[0].add(m.getRateInt());
            }
            ArrayAdapter<String> ra = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, rateLabels);
            ra.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerRate.setAdapter(ra);
            int savedHz = prefs.getInt("oc_target_hz", -1);
            int idx = rateHolder[0].indexOf(savedHz);
            if (!rateHolder[0].isEmpty()) {
                spinnerRate.setSelection(idx >= 0 ? idx : 0);
                if (savedHz < 0) prefs.edit().putInt("oc_target_hz", rateHolder[0].get(idx >= 0 ? idx : 0)).apply();
            }
        };
        populateRates.run();
        spinnerRes.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View vv, int pos, long id) {
                if (pos < 0 || pos >= resKeys.size()) return;
                prefs.edit().putString("oc_target_res", resKeys.get(pos)).apply();
                populateRates.run();
                updateRunningTarget(resKeys.get(pos), prefs.getInt("oc_target_hz", -1));
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        spinnerRate.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, android.view.View vv, int pos, long id) {
                if (pos < 0 || pos >= rateHolder[0].size()) return;
                int hz = rateHolder[0].get(pos);
                prefs.edit().putInt("oc_target_hz", hz).apply();
                updateRunningTarget(prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0)), hz);
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) {}
        });
        mute[0] = true;
        boolean ocOn = prefs.getBoolean("auto_overclock", false) && AutoOverclockManager.isRunning();
        switchOC.setChecked(ocOn);
        switchCustom.setChecked(prefs.getBoolean("custom_app_refresh", false));
        showOcTargetContainer(ocOn);
        if (ocOn) {
            tvOCStatus.setText(R.string.guard_enabled);
            tvOCStatus.setTextColor(0xFF2ECC71);
            startOverclockStatusUpdater();
        }
        mute[0] = false;
        switchCustom.setOnCheckedChangeListener((btn, checked) -> {
            if (mute[0]) return;
            if (checked) {
                if (!ensureBasePermission(switchCustom)) return;
                prefs.edit().putBoolean("custom_app_refresh", true).apply();
            } else {
                prefs.edit().putBoolean("custom_app_refresh", false).apply();
            }
        });
        switchOC.setOnCheckedChangeListener((btn, checked) -> {
            if (mute[0]) return;
            prefs.edit().putBoolean("auto_overclock", checked).apply();
            if (checked) {
                if (Build.VERSION.SDK_INT >= 33 &&
                        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 200);
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                    return;
                }
                if (!ensureBasePermission(switchOC)) return;
                String ocRes = prefs.getString("oc_target_res", resKeys.isEmpty() ? "" : resKeys.get(0));
                int ocHz = prefs.getInt("oc_target_hz", -1);
                if (ocRes.isEmpty() && !resKeys.isEmpty()) {
                    ocRes = resKeys.get(Math.max(0, spinnerRes.getSelectedItemPosition()));
                }
                if (ocHz < 0) {
                    try {
                        String lbl = (String) spinnerRate.getSelectedItem();
                        if (lbl != null) ocHz = Integer.parseInt(lbl.replace(" Hz", "").trim());
                    } catch (Exception ignored) {}
                }
                if (ocRes.isEmpty() || ocHz < 0) {
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                    return;
                }
                prefs.edit().putString("oc_target_res", ocRes).putInt("oc_target_hz", ocHz).apply();
                prefs.edit().putBoolean("auto_overclock", true).apply();
                String[] wh = ocRes.split("x");
                try {
                    int tw = Integer.parseInt(wh[0]), th = Integer.parseInt(wh[1]);
                    AutoOverclockManager.startService(requireContext(), prefs.getString("auth_mode", ""), tw, th, ocHz);
                    tvOCStatus.setText(R.string.guard_enabled);
                    tvOCStatus.setTextColor(0xFF2ECC71);
                    showOcTargetContainer(true);
                    startOverclockStatusUpdater();
                } catch (Exception e) {
                    mute[0] = true;
                    switchOC.setChecked(false);
                    mute[0] = false;
                    prefs.edit().putBoolean("auto_overclock", false).apply();
                }
            } else {
                AutoOverclockManager.stopService(requireContext());
                stopOverclockStatusUpdater();
                tvOCStatus.setText(R.string.guard_disabled);
                tvOCStatus.setTextColor(0xFFE74C3C);
                showOcTargetContainer(false);
            }
        });
    }
    private boolean ensureBasePermission(SwitchCompat changedSwitch) {
        if (!isAccessibilityServiceEnabled()) {
            Toast.makeText(getContext(), R.string.accessibility_required, Toast.LENGTH_LONG).show();
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (Exception e) { Toast.makeText(getContext(), R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show(); }
            changedSwitch.setChecked(false);
            return false;
        }
        String mode = prefs.getString("auth_mode", "");
        if (mode.isEmpty()) {
            Toast.makeText(getContext(), R.string.no_any_permission, Toast.LENGTH_SHORT).show();
            changedSwitch.setChecked(false);
            return false;
        }
        return true;
    }
    private void updateRunningTarget(String res, int hz) {
        if (!AutoOverclockManager.isRunning() || res == null || res.isEmpty() || hz <= 0) return;
        String[] wh = res.split("x");
        if (wh.length != 2) return;
        try {
            AutoOverclockManager.updateTarget(Integer.parseInt(wh[0]), Integer.parseInt(wh[1]), hz);
        } catch (Exception ignored) {}
    }
    private void showOcTargetContainer(boolean show) {
        if (ocTargetContainer != null) ocTargetContainer.setVisibility(View.VISIBLE);
    }
    private void updateCurrentDisplayText() {
        if (tvOCCur == null || !isAdded()) return;
        float r = AutoOverclockManager.getCurrentRate(requireContext());
        String res = AutoOverclockManager.getCurrentResolution(requireContext());
        tvOCCur.setText(getString(R.string.current_rate_format, Math.round(r), res));
    }
    private void startOverclockStatusUpdater() {
        stopOverclockStatusUpdater();
        overclockHandler = new Handler(Looper.getMainLooper());
        overclockUpdater = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;
                updateCurrentDisplayText();
                if (AutoOverclockManager.isRunning()) {
                    tvOCStatus.setText(getString(R.string.guard_running_format, AutoOverclockManager.getLastLog()));
                    tvOCStatus.setTextColor(0xFF2ECC71);
                    updateUI();
                    overclockHandler.postDelayed(this, 1500);
                }
            }
        };
        overclockHandler.post(overclockUpdater);
    }
    private void stopOverclockStatusUpdater() {
        if (overclockHandler != null && overclockUpdater != null) {
            overclockHandler.removeCallbacks(overclockUpdater);
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(getContext());
    }
    private void updateUI() {
        if (prefs == null) return;
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float uiAlpha = 1f - wallpaperAlpha;
        if (titleBar != null) titleBar.setAlpha(uiAlpha);
        if (titleText != null) titleText.setAlpha(uiAlpha);
        float cardA = 1f - (prefs.getInt("custom_card_alpha", 0) / 100f);
        float textA = 1f - (prefs.getInt("custom_text_alpha", 0) / 100f);
        android.view.View root = getView();
        if (root instanceof android.view.ViewGroup) applyCustomAlpha((android.view.ViewGroup) root, cardA, textA);
        if (tvOCStatus != null) tvOCStatus.setAlpha(1f);
        if (tvOCCur != null) tvOCCur.setAlpha(1f);
    }
    private void applyCustomAlpha(android.view.ViewGroup vg, float cardA, float textA) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            android.view.View c = vg.getChildAt(i);
            if (c instanceof androidx.cardview.widget.CardView) {
                c.setAlpha(cardA);
            } else if (c instanceof android.widget.TextView) {
                if (c != tvOCStatus && c != tvOCCur) c.setAlpha(textA);
            }
            if (c instanceof android.view.ViewGroup) applyCustomAlpha((android.view.ViewGroup) c, cardA, textA);
        }
    }
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("wallpaper_alpha".equals(key)) updateUI();
    }
    @Override
    public void onResume() {
        super.onResume();
        updateCurrentDisplayText();
        refreshEnabledList();
        updateUI();
    }
    private void refreshEnabledList() {
        if (llEnabledApps == null || !isAdded()) return;
        llEnabledApps.removeAllViews();
        android.content.pm.PackageManager pm = requireContext().getPackageManager();
        java.util.Map<String, ?> allPrefs = prefs.getAll();
        java.util.List<String> enabledPkgs = new java.util.ArrayList<>();
        for (String key : allPrefs.keySet()) {
            if (key.startsWith("app_refresh_enabled_")) {
                Object val = allPrefs.get(key);
                if (Boolean.TRUE.equals(val)) {
                    enabledPkgs.add(key.substring("app_refresh_enabled_".length()));
                }
            }
        }
        if (enabledPkgs.isEmpty()) {
            if (tvEnabledEmpty != null) tvEnabledEmpty.setVisibility(View.VISIBLE);
            return;
        }
        if (tvEnabledEmpty != null) tvEnabledEmpty.setVisibility(View.GONE);
        int dp12 = (int)(12 * getResources().getDisplayMetrics().density);
        int dp8  = (int)(8  * getResources().getDisplayMetrics().density);
        int dp46 = (int)(46 * getResources().getDisplayMetrics().density);
        for (String pkg : enabledPkgs) {
            String res = prefs.getString("app_refresh_res_" + pkg, "");
            int hz = prefs.getInt("app_refresh_hz_" + pkg, -1);
            android.widget.LinearLayout row = new android.widget.LinearLayout(getContext());
            row.setOrientation(android.widget.LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setPadding(dp12, dp8, dp12, dp8);
            row.setClickable(true);
            row.setFocusable(true);
            row.setBackground(android.util.TypedValue.applyDimension(
                android.util.TypedValue.COMPLEX_UNIT_DIP, 0, getResources().getDisplayMetrics()) >= 0
                ? requireContext().obtainStyledAttributes(new int[]{android.R.attr.selectableItemBackground})
                    .getDrawable(0)
                : null);
            android.widget.ImageView icon = new android.widget.ImageView(getContext());
            android.widget.LinearLayout.LayoutParams iconLp =
                new android.widget.LinearLayout.LayoutParams(dp46, dp46);
            icon.setLayoutParams(iconLp);
            try {
                android.content.pm.ApplicationInfo ai = pm.getApplicationInfo(pkg, 0);
                icon.setImageDrawable(pm.getApplicationIcon(ai));
            } catch (Exception e) {
                icon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            row.addView(icon);
            android.widget.LinearLayout textBlock = new android.widget.LinearLayout(getContext());
            textBlock.setOrientation(android.widget.LinearLayout.VERTICAL);
            android.widget.LinearLayout.LayoutParams tbLp =
                new android.widget.LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tbLp.setMarginStart(dp12);
            textBlock.setLayoutParams(tbLp);
            android.widget.TextView tvName = new android.widget.TextView(getContext());
            String appLabel = pkg;
            try { appLabel = pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString(); } catch (Exception ignored) {}
            tvName.setText(appLabel);
            tvName.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 16);
            tvName.setTextColor(0xFF1A1A2E);
            tvName.setTypeface(null, android.graphics.Typeface.BOLD);
            tvName.setMaxLines(1);
            tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textBlock.addView(tvName);
            android.widget.TextView tvPkg = new android.widget.TextView(getContext());
            tvPkg.setText(pkg);
            tvPkg.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
            tvPkg.setTextColor(0xFF888888);
            tvPkg.setMaxLines(1);
            tvPkg.setEllipsize(android.text.TextUtils.TruncateAt.END);
            textBlock.addView(tvPkg);
            android.widget.TextView tvRate = new android.widget.TextView(getContext());
            String rateInfo = (res.isEmpty() ? "?" : res) + (hz > 0 ? " @ " + hz + " Hz" : "");
            tvRate.setText(rateInfo);
            tvRate.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 13);
            tvRate.setTextColor(0xFF1976D2);
            textBlock.addView(tvRate);
            row.addView(textBlock);
            final String finalPkg = pkg;
            row.setOnClickListener(v -> {
                try {
                    android.content.Intent intent = new android.content.Intent(requireContext(),
                        com.yxlanyu.refreshrate.AppRefreshConfigActivity.class);
                    intent.putExtra("pkg", finalPkg);
                    startActivity(intent);
                    requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } catch (Exception ignored) {}
            });
            llEnabledApps.addView(row);
            if (enabledPkgs.indexOf(pkg) < enabledPkgs.size() - 1) {
                android.view.View div = new android.view.View(getContext());
                android.widget.LinearLayout.LayoutParams divLp =
                    new android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT, 1);
                divLp.setMarginStart(dp46 + dp12 + dp12);
                div.setLayoutParams(divLp);
                div.setBackgroundColor(0x1A000000);
                llEnabledApps.addView(div);
            }
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopOverclockStatusUpdater();
        if (prefs != null) prefs.unregisterOnSharedPreferenceChangeListener(this);
        titleBar = null;
        titleText = null;
        tvOCStatus = null;
        tvOCCur = null;
        ocTargetContainer = null;
        llEnabledApps = null;
        tvEnabledEmpty = null;
    }
}
