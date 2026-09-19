package com.yxlanyu.refreshrate.fragment;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.yxlanyu.refreshrate.AppearanceActivity;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.util.RootUtils;
import com.yxlanyu.refreshrate.util.AutoOverclockManager;
import com.yxlanyu.refreshrate.util.AccessibilityUtils;
import java.util.*;
public class SettingsFragment extends Fragment {
    private SharedPreferences prefs;
    private View rootView;
    private Handler overclockHandler;
    private Runnable overclockUpdater;
    private void startOverclockStatusUpdater(TextView tvStatus, TextView tvCur) {
        overclockHandler = new Handler(Looper.getMainLooper());
        overclockUpdater = new Runnable() {
            @Override public void run() {
                if (!isAdded()) return;
                float rate = AutoOverclockManager.getCurrentRate(requireContext());
                String res  = AutoOverclockManager.getCurrentResolution(requireContext());
                tvCur.setText(getString(R.string.current_rate_format, Math.round(rate), res));
                if (AutoOverclockManager.isRunning()) {
                    tvStatus.setText(getString(R.string.guard_running_format, AutoOverclockManager.getLastLog()));
                    tvStatus.setTextColor(0xFF2ECC71);
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
    @Override public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_settings, c, false);
        rootView = v;
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        View rowAppearance = v.findViewById(R.id.row_appearance);
        if (rowAppearance != null) {
            rowAppearance.setOnClickListener(view -> {
                Intent intent = new Intent(getActivity(), AppearanceActivity.class);
                startActivity(intent);
            });
        }
        View rowAnimEffect = v.findViewById(R.id.row_anim_effect);
        if (rowAnimEffect != null) {
            rowAnimEffect.setOnClickListener(view -> startActivity(new Intent(getActivity(), com.yxlanyu.refreshrate.AnimationActivity.class)));
        }
        View rowLanguage = v.findViewById(R.id.row_language);
        if (rowLanguage != null) {
            rowLanguage.setOnClickListener(view -> {
                startActivity(new Intent(getActivity(), com.yxlanyu.refreshrate.LanguageActivity.class));
            });
        }
        androidx.appcompat.widget.SwitchCompat switchNativeRefresh = v.findViewById(R.id.switch_native_refresh);
        if (switchNativeRefresh != null) {
            switchNativeRefresh.setChecked(prefs.getBoolean("native_refresh_overlay", false));
            switchNativeRefresh.setOnCheckedChangeListener((btn, checked) -> {
                final String mode = prefs.getString("auth_mode", "");
                final boolean useRoot = "root".equals(mode);
                final boolean useShizuku = "shizuku".equals(mode);
                if (!useRoot && !useShizuku) {
                    Toast.makeText(getContext(), R.string.rate_lock_need_auth, Toast.LENGTH_SHORT).show();
                    btn.setChecked(false);
                    return;
                }
                prefs.edit().putBoolean("native_refresh_overlay", checked).apply();
                new Thread(() -> {
                    if (useRoot) RootUtils.setNativeRefreshOverlay(checked);
                    else com.yxlanyu.refreshrate.util.ShizukuUtils.setNativeRefreshOverlay(checked);
                }).start();
            });
        }
        TextView rootStatus    = v.findViewById(R.id.tv_root_status);
        TextView shizukuStatus = v.findViewById(R.id.tv_shizuku_status);
        Button btnRequestShizuku = v.findViewById(R.id.btn_request_shizuku);
        androidx.appcompat.widget.SwitchCompat switchRoot    = v.findViewById(R.id.switch_root);
        androidx.appcompat.widget.SwitchCompat switchShizuku = v.findViewById(R.id.switch_shizuku);
        String savedMode = prefs.getString("auth_mode", "");
        switchRoot.setChecked("root".equals(savedMode));
        switchShizuku.setChecked("shizuku".equals(savedMode));
        new Thread(() -> {
            boolean ok = RootUtils.isRooted();
            if (isAdded() && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    rootStatus.setText(ok ? R.string.settings_root_granted : R.string.settings_root_denied);
                    rootStatus.setTextColor(ok ? 0xFF2ECC71 : 0xFFE74C3C);
                });
            }
        }).start();
        updateShizukuStatus(shizukuStatus, btnRequestShizuku);
        btnRequestShizuku.setOnClickListener(view -> {
            if (com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable()) {
                com.yxlanyu.refreshrate.util.ShizukuUtils.requestPermission();
                new Handler(Looper.getMainLooper())
                    .postDelayed(() -> updateShizukuStatus(shizukuStatus, btnRequestShizuku), 1000);
            } else {
                Toast.makeText(getContext(), R.string.shizuku_install_hint, Toast.LENGTH_LONG).show();
            }
        });
        View rowAccessibility = v.findViewById(R.id.row_accessibility);
        if (rowAccessibility != null) {
            rowAccessibility.setOnClickListener(view -> {
                try {
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                } catch (Exception e) {
                    Toast.makeText(getContext(), R.string.accessibility_open_failed, Toast.LENGTH_SHORT).show();
                }
            });
        }
        View btnYxlanyu = v.findViewById(R.id.btn_yxlanyu_link);
        if (btnYxlanyu != null) {
            btnYxlanyu.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/1779";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        switchRoot.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                switchShizuku.setChecked(false);
                prefs.edit().putString("auth_mode", "root").apply();
            } else {
                if (!switchShizuku.isChecked()) {
                    prefs.edit().putString("auth_mode", "").apply();
                }
            }
        });
        switchShizuku.setOnCheckedChangeListener((btn, checked) -> {
            if (checked) {
                switchRoot.setChecked(false);
                prefs.edit().putString("auth_mode", "shizuku").apply();
                updateShizukuStatus(shizukuStatus, btnRequestShizuku);
            } else {
                if (!switchRoot.isChecked()) {
                    prefs.edit().putString("auth_mode", "").apply();
                }
                btnRequestShizuku.setVisibility(android.view.View.GONE);
            }
        });
        View btnAihaozhe = v.findViewById(R.id.btn_aihaozhe_link);
        if (btnAihaozhe != null) {
            btnAihaozhe.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/31452988";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        View btnShagua = v.findViewById(R.id.btn_shagua_link);
        if (btnShagua != null) {
            btnShagua.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/33802586";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        View btnHuaiyin = v.findViewById(R.id.btn_huaiyin_link);
        if (btnHuaiyin != null) {
            btnHuaiyin.setOnClickListener(view -> {
                String url = "https://www.coolapk.com/u/14621568";
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage("com.coolapk.market"));
                } catch (Exception e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                }
            });
        }
        applySettingTransparency();
        Button btnGenerateLog = v.findViewById(R.id.btn_generate_log);
        if (btnGenerateLog != null) {
            btnGenerateLog.setOnClickListener(view -> {
                new Thread(() -> {
                    String log = RootUtils.generateRuntimeLog(requireContext().getApplicationContext());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> showLogDialog(log));
                    }
                }).start();
            });
        }
        androidx.core.widget.NestedScrollView settingsNsv = v.findViewById(R.id.settings_scroll_view);
        if (settingsNsv != null) {
            settingsNsv.setOnScrollChangeListener((androidx.core.widget.NestedScrollView.OnScrollChangeListener)
                (nsv, scrollX, scrollY, oldScrollX, oldScrollY) -> {
                    if (!(getActivity() instanceof com.yxlanyu.refreshrate.MainActivity)) return;
                    com.yxlanyu.refreshrate.MainActivity ma = (com.yxlanyu.refreshrate.MainActivity) getActivity();
                    if (scrollY > oldScrollY + 8) ma.hideBottomNav();
                    else if (scrollY < oldScrollY - 8) ma.showBottomNav();
                });
        }
        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        applySettingTransparency();
        View v = getView();
        if (v != null) {
            TextView tvA11yStatus = v.findViewById(R.id.tv_accessibility_status);
            updateAccessibilityStatus(tvA11yStatus);
        }
    }
    private boolean isAccessibilityServiceEnabled() {
        return AccessibilityUtils.isKeepAliveServiceEnabled(getContext());
    }
    private void updateAccessibilityStatus(TextView tv) {
        if (tv == null) return;
        boolean enabled = isAccessibilityServiceEnabled();
        tv.setText(enabled ? R.string.accessibility_enabled : R.string.accessibility_disabled);
        tv.setTextColor(enabled ? 0xFF2ECC71 : 0xFFE74C3C);
    }
    private void updateShizukuStatus(TextView tv, Button btn) {
        boolean avail = com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable();
        boolean perm  = com.yxlanyu.refreshrate.util.ShizukuUtils.hasPermission();
        if (!avail) {
            tv.setText(R.string.shizuku_not_running);
            tv.setTextColor(0xFF888888);
            btn.setVisibility(android.view.View.GONE);
        } else if (!perm) {
            tv.setText(R.string.shizuku_no_perm);
            tv.setTextColor(0xFFE74C3C);
            btn.setVisibility(android.view.View.VISIBLE);
        } else {
            tv.setText(R.string.shizuku_authorized);
            tv.setTextColor(0xFF2ECC71);
            btn.setVisibility(android.view.View.GONE);
        }
    }
    private void applySettingTransparency() {
        if (rootView == null) return;
        int cardAlpha = 255 - (int)(prefs.getInt("setting_card_alpha", 0) / 100f * 255);
        int textAlpha = 255 - (int)(prefs.getInt("setting_text_alpha", 0) / 100f * 255);
        applyAlphaToViewGroup((ViewGroup) rootView, cardAlpha, textAlpha);
    }
    private void applyAlphaToViewGroup(ViewGroup parent, int cardAlpha, int textAlpha) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            if (child instanceof CardView) {
                CardView cv = (CardView) child;
                cv.setCardBackgroundColor(Color.argb(cardAlpha, 255, 255, 255));
                cv.setCardElevation(0f);
                cv.setMaxCardElevation(0f);
            } else if (child instanceof TextView) {
                child.setAlpha(textAlpha / 255f);
            }
            if (child instanceof ViewGroup) {
                applyAlphaToViewGroup((ViewGroup) child, cardAlpha, textAlpha);
            }
        }
    }
    @Override public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 200) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            } else {
                Toast.makeText(getContext(), R.string.notification_perm_denied, Toast.LENGTH_LONG).show();
            }
        }
    }
    private void showLogDialog(String logContent) {
        if (getContext() == null) return;
        java.io.File logDir = new java.io.File(requireContext().getCacheDir(), "logs");
        logDir.mkdirs();
        java.io.File logFile = new java.io.File(logDir, "refresh_rate_log.txt");
        try {
            java.io.FileWriter fw = new java.io.FileWriter(logFile);
            fw.write(logContent); fw.close();
        } catch (Exception ignored) {}
        ScrollView sv = new ScrollView(getContext());
        TextView tv = new TextView(getContext());
        tv.setText(logContent);
        tv.setPadding(30, 30, 30, 30);
        tv.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setTextColor(Color.BLACK);
        sv.addView(tv);
        new AlertDialog.Builder(getContext())
                .setTitle(R.string.log_dialog_title)
                .setView(sv)
                .setPositiveButton(R.string.log_dialog_close, null)
                .setNeutralButton(R.string.log_dialog_share, (d, w) -> {
                    try {
                        android.net.Uri fileUri = androidx.core.content.FileProvider.getUriForFile(
                            requireContext(),
                            requireContext().getPackageName() + ".fileprovider",
                            logFile);
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_STREAM, fileUri);
                        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(share, getString(R.string.log_dialog_share)));
                    } catch (Exception e) {
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.setType("text/plain");
                        share.putExtra(Intent.EXTRA_TEXT, logContent);
                        startActivity(Intent.createChooser(share, getString(R.string.log_dialog_share)));
                    }
                })
                .show();
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        stopOverclockStatusUpdater();
        rootView = null;
    }
}
