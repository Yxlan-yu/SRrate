package com.yxlanyu.refreshrate;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.io.InputStream;
public class AppearanceActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private ImageView wallpaperView;
    private String pendingPageKey;
    private ActivityResultLauncher<Intent> pickImageLauncher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_appearance);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_appearance);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        wallpaperView = findViewById(R.id.appearance_wallpaper);
        applyWallpaper();
        setupNavSchemeRow();
        setupWallpaper();
        setupCardDisplay();
        pickImageLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null && pendingPageKey != null) {
                        boolean saved = WallpaperStore.savePageFromUri(this, uri, pendingPageKey);
                        if (saved) {
                            prefs.edit().putInt("wallpaper_type", 3).apply();
                            refreshPresetCards();
                            updateCustomBgVisibility();
                            refreshAllBgCards();
                            applyWallpaper();
                            Toast.makeText(this, R.string.custom_wallpaper_saved, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, R.string.update_check_fail, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        );
    }
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    protected void onResume() {
        super.onResume();
        applyWallpaper();
    }
    private void applyWallpaper() {
        if (wallpaperView == null) return;
        int type = prefs.getInt("wallpaper_type", 4);
        float alpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        wallpaperView.setAlpha(alpha);
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, "settings");
            if (bm != null) { wallpaperView.setImageBitmap(bm); return; }
            wallpaperView.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white));
            return;
        }
        wallpaperView.setImageDrawable(WallpaperStore.presetDrawable(this, type));
    }
    private void setupNavSchemeRow() {
        TextView tvCurrent = findViewById(R.id.tv_nav_style_current);
        if (tvCurrent == null) return;
        updateNavSchemeText(tvCurrent);
        android.view.View row = findViewById(R.id.row_nav_style);
        if (row != null) row.setOnClickListener(v -> showNavSchemeDialog(tvCurrent));
    }
    private void updateNavSchemeText(TextView tv) {
        int scheme = prefs.getInt("nav_scheme", 0);
        int[] names = { R.string.nav_scheme_float, R.string.nav_scheme_classic, R.string.nav_scheme_top, R.string.nav_scheme_left, R.string.nav_scheme_right };
        if (scheme < 0 || scheme >= names.length) scheme = 0;
        tv.setText(getString(names[scheme]));
    }
    private void showNavSchemeDialog(TextView tvCurrent) {
    int current = prefs.getInt("nav_scheme", 0);
    android.view.View dialogView = android.view.LayoutInflater.from(this)
        .inflate(R.layout.dialog_nav_scheme, null);
    android.widget.RadioGroup rg = dialogView.findViewById(R.id.rg_nav_scheme);
    int[] rbIds = { R.id.rb_nav_float, R.id.rb_nav_classic, R.id.rb_nav_top, R.id.rb_nav_left, R.id.rb_nav_right };
    rg.check(rbIds[(current >= 0 && current < rbIds.length) ? current : 0]);
    AlertDialog dialog = new AlertDialog.Builder(this)
        .setView(dialogView)
        .create();
    if (dialog.getWindow() != null) {
        dialog.getWindow().setBackgroundDrawable(
            new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
    }
    rg.setOnCheckedChangeListener((group, checkedId) -> {
        int which = checkedId == R.id.rb_nav_classic ? 1 : checkedId == R.id.rb_nav_top ? 2 : checkedId == R.id.rb_nav_left ? 3 : checkedId == R.id.rb_nav_right ? 4 : 0;
        prefs.edit().putInt("nav_scheme", which).apply();
        updateNavSchemeText(tvCurrent);
        dialog.dismiss();
    });
    dialog.show();
}
    private void setupWallpaper() {
        SeekBar sbAlpha = findViewById(R.id.sb_alpha);
        final android.widget.TextView tvAlphaPct = findViewById(R.id.tv_alpha_pct);
        if (sbAlpha == null) return;
        int initP = (int)(prefs.getFloat("wallpaper_alpha", 0.3f) * 100);
        sbAlpha.setProgress(initP);
        if (tvAlphaPct != null) tvAlphaPct.setText(initP + "%");
        sbAlpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                float alpha = p / 100f;
                prefs.edit().putFloat("wallpaper_alpha", alpha).apply();
                if (wallpaperView != null) wallpaperView.setAlpha(alpha);
                if (tvAlphaPct != null) tvAlphaPct.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) { applyWallpaper(); }
        });
        int[][] cards = {{R.id.wp_custom,3},{R.id.wp_white,4},{R.id.wp_black,5},{R.id.wp_sunset,6},{R.id.wp_aurora,1},{R.id.wp_emerald,2},{R.id.wp_sky,7},{R.id.wp_pink,8},{R.id.wp_mint,9},{R.id.wp_purple,10}};
        for (int[] c : cards) {
            final int t = c[1];
            android.view.View v = findViewById(c[0]);
            if (v != null) v.setOnClickListener(x -> selectWpType(t));
        }
        refreshPresetCards();
        setupBgCard("home", R.id.iv_bg_home, R.id.btn_clear_home, R.id.btn_pick_home);
        setupBgCard("app", R.id.iv_bg_app, R.id.btn_clear_app, R.id.btn_pick_app);
        setupBgCard("remote", R.id.iv_bg_remote, R.id.btn_clear_remote, R.id.btn_pick_remote);
        setupBgCard("settings", R.id.iv_bg_settings, R.id.btn_clear_settings, R.id.btn_pick_settings);
        updateCustomBgVisibility();
    }
    private void selectWpType(int type) {
        prefs.edit().putInt("wallpaper_type", type).apply();
        refreshPresetCards();
        updateCustomBgVisibility();
        applyWallpaper();
    }
    private void refreshPresetCards() {
        int cur = prefs.getInt("wallpaper_type", 4);
        styleCard(R.id.wp_custom, cur == 3);
        styleCard(R.id.wp_white, cur == 4);
        styleCard(R.id.wp_black, cur == 5);
        styleCard(R.id.wp_sunset, cur == 6);
        styleCard(R.id.wp_aurora, cur == 1);
        styleCard(R.id.wp_emerald, cur == 2);
        styleCard(R.id.wp_sky, cur == 7);
        styleCard(R.id.wp_pink, cur == 8);
        styleCard(R.id.wp_mint, cur == 9);
        styleCard(R.id.wp_purple, cur == 10);
    }
    private void styleCard(int id, boolean sel) {
        android.widget.TextView tv = findViewById(id);
        if (tv == null) return;
        tv.setBackgroundResource(sel ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tv.setTextColor(sel ? 0xFFFFFFFF : 0xFF546E7A);
        tv.setCompoundDrawablesWithIntrinsicBounds(0, 0, sel ? R.drawable.ic_check_white : 0, 0);
    }
    private void setupBgCard(String key, int ivId, int clearId, int pickId) {
        android.widget.ImageView iv = findViewById(ivId);
        android.view.View clear = findViewById(clearId);
        android.view.View pick = findViewById(pickId);
        refreshBgCard(key, iv, clear);
        if (pick != null) pick.setOnClickListener(v -> { pendingPageKey = key; pickPageImage(); });
        if (clear != null) clear.setOnClickListener(v -> { WallpaperStore.clearPage(this, key); refreshBgCard(key, iv, clear); });
    }
    private void refreshBgCard(String key, android.widget.ImageView iv, android.view.View clear) {
        boolean has = WallpaperStore.hasPage(this, key);
        if (iv != null) {
            if (has) { iv.setImageBitmap(WallpaperStore.loadPage(this, key)); iv.setVisibility(android.view.View.VISIBLE); }
            else iv.setVisibility(android.view.View.GONE);
        }
        if (clear != null) clear.setVisibility(has ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void refreshAllBgCards() {
        refreshBgCard("home", findViewById(R.id.iv_bg_home), findViewById(R.id.btn_clear_home));
        refreshBgCard("app", findViewById(R.id.iv_bg_app), findViewById(R.id.btn_clear_app));
        refreshBgCard("remote", findViewById(R.id.iv_bg_remote), findViewById(R.id.btn_clear_remote));
        refreshBgCard("settings", findViewById(R.id.iv_bg_settings), findViewById(R.id.btn_clear_settings));
    }
    private void updateCustomBgVisibility() {
        android.view.View ll = findViewById(R.id.ll_custom_bg);
        if (ll != null) ll.setVisibility(prefs.getInt("wallpaper_type", 4) == 3 ? android.view.View.VISIBLE : android.view.View.GONE);
    }
    private void pickPageImage() {
        String perm = Build.VERSION.SDK_INT >= 33
            ? Manifest.permission.READ_MEDIA_IMAGES
            : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{perm}, 100);
        } else {
            openGallery();
        }
    }
    private void setupCardDisplay() {
        SeekBar sbCard = findViewById(R.id.sb_card_alpha);
        if (sbCard == null) return;
        final android.widget.TextView pctCard = findViewById(R.id.tv_pct_card);
        int initCard = prefs.getInt("card_alpha", 0);
        sbCard.setProgress(initCard);
        if (pctCard != null) pctCard.setText(initCard + "%");
        sbCard.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                prefs.edit().putInt("card_alpha", p).apply();
                if (pctCard != null) pctCard.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
        SeekBar sbSettingCard = findViewById(R.id.sb_setting_card_alpha);
        SeekBar sbSettingText = findViewById(R.id.sb_setting_text_alpha);
        final android.widget.TextView pctSettingCard = findViewById(R.id.tv_pct_setting_card);
        final android.widget.TextView pctSettingText = findViewById(R.id.tv_pct_setting_text);
        applySettingCardPreview(
            prefs.getInt("setting_card_alpha", 0),
            prefs.getInt("setting_text_alpha", 0));
        if (sbSettingCard != null) {
            int initSC = prefs.getInt("setting_card_alpha", 0);
            sbSettingCard.setProgress(initSC);
            if (pctSettingCard != null) pctSettingCard.setText(initSC + "%");
            sbSettingCard.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                    int textP = sbSettingText != null ? sbSettingText.getProgress() : prefs.getInt("setting_text_alpha", 0);
                    prefs.edit().putInt("setting_card_alpha", p).apply();
                    applySettingCardPreview(p, textP);
                    if (pctSettingCard != null) pctSettingCard.setText(p + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        if (sbSettingText != null) {
            int initST = prefs.getInt("setting_text_alpha", 0);
            sbSettingText.setProgress(initST);
            if (pctSettingText != null) pctSettingText.setText(initST + "%");
            sbSettingText.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                    int cardP = sbSettingCard != null ? sbSettingCard.getProgress() : prefs.getInt("setting_card_alpha", 0);
                    prefs.edit().putInt("setting_text_alpha", p).apply();
                    applySettingCardPreview(cardP, p);
                    if (pctSettingText != null) pctSettingText.setText(p + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar s) {}
                @Override public void onStopTrackingTouch(SeekBar s) {}
            });
        }
        bindAlphaSeek(R.id.sb_remote_card_alpha, "remote_card_alpha", R.id.tv_pct_remote_card);
        bindAlphaSeek(R.id.sb_remote_text_alpha, "remote_text_alpha", R.id.tv_pct_remote_text);
        bindAlphaSeek(R.id.sb_custom_card_alpha, "custom_card_alpha", R.id.tv_pct_custom_card);
        bindAlphaSeek(R.id.sb_custom_text_alpha, "custom_text_alpha", R.id.tv_pct_custom_text);
    }
    private void bindAlphaSeek(int id, final String key, int pctId) {
        SeekBar sb = findViewById(id);
        if (sb == null) return;
        final android.widget.TextView pct = findViewById(pctId);
        int init = prefs.getInt(key, 0);
        sb.setProgress(init);
        if (pct != null) pct.setText(init + "%");
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int p, boolean f) {
                prefs.edit().putInt(key, p).apply();
                if (pct != null) pct.setText(p + "%");
            }
            @Override public void onStartTrackingTouch(SeekBar s) {}
            @Override public void onStopTrackingTouch(SeekBar s) {}
        });
    }
    private void applySettingCardPreview(int cardProgress, int textProgress) {
        int cardAlpha = 255 - (int)(cardProgress / 100f * 255);
        int textAlpha = 255 - (int)(textProgress / 100f * 255);
        android.view.View content = findViewById(android.R.id.content);
        if (content instanceof android.view.ViewGroup) {
            applyAlphaToViewGroup((android.view.ViewGroup) content, cardAlpha, textAlpha);
        }
    }
    private void applyAlphaToViewGroup(android.view.ViewGroup parent, int cardAlpha, int textAlpha) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof androidx.cardview.widget.CardView) {
                androidx.cardview.widget.CardView cv = (androidx.cardview.widget.CardView) child;
                cv.setCardBackgroundColor(android.graphics.Color.argb(cardAlpha, 255, 255, 255));
                cv.setCardElevation(0f);
                cv.setMaxCardElevation(0f);
            } else if (child instanceof android.widget.TextView) {
                child.setAlpha(textAlpha / 255f);
            }
            if (child instanceof android.view.ViewGroup) {
                applyAlphaToViewGroup((android.view.ViewGroup) child, cardAlpha, textAlpha);
            }
        }
    }
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                if (!shouldShowRequestPermissionRationale(permissions[0])) {
                    new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_denied_title)
                        .setMessage(R.string.permission_denied_message)
                        .setPositiveButton(R.string.permission_go_settings, (d, w) -> {
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            i.setData(Uri.parse("package:" + getPackageName()));
                            startActivity(i);
                        })
                        .setNegativeButton(R.string.permission_cancel, null)
                        .show();
                } else {
                    Toast.makeText(this, R.string.need_storage_permission, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
