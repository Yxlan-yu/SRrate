package com.yxlanyu.refreshrate;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
public class AnimationActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private interface OnPick { void pick(int index); }
    @Override protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        com.yxlanyu.refreshrate.util.LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_animation);
        applyWallpaperBg((android.widget.ImageView) findViewById(R.id.anim_wallpaper));
        applyCardAlpha((androidx.cardview.widget.CardView) findViewById(R.id.anim_card));
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar_animation);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        toolbar.setNavigationOnClickListener(x -> finish());
        final android.content.SharedPreferences sp = getSharedPreferences("s", MODE_PRIVATE);
        androidx.appcompat.widget.SwitchCompat swAnim = findViewById(R.id.switch_anim_engine);
        if (swAnim != null) {
            swAnim.setChecked(sp.getBoolean("anim_engine", true));
            swAnim.setOnCheckedChangeListener((b, on) -> sp.edit().putBoolean("anim_engine", on).apply());
        }
        final String[] typeKeys = {"spring", "linear", "fade", "slide"};
        final String[] typeNames = { getString(R.string.anim_type_spring), getString(R.string.anim_type_linear), getString(R.string.anim_type_fade), getString(R.string.anim_type_slide) };
        final android.widget.TextView tvType = findViewById(R.id.tv_anim_type_value);
        final android.view.View rowType = findViewById(R.id.row_anim_type);
        if (tvType != null && rowType != null) {
            String cur = sp.getString("anim_type", "spring");
            int ci = 0; for (int i = 0; i < typeKeys.length; i++) if (typeKeys[i].equals(cur)) ci = i;
            tvType.setText(typeNames[ci]);
            final int[] sel = { ci };
            rowType.setOnClickListener(x -> showStyledChoice(getString(R.string.anim_type_title), typeNames, sel[0], idx -> {
                sel[0] = idx; sp.edit().putString("anim_type", typeKeys[idx]).apply(); tvType.setText(typeNames[idx]);
            }));
        }
        final float[] spd = {0.5f, 0.75f, 1.0f, 1.5f, 2.0f};
        final String[] spdNames = {"0.5x", "0.75x", "1.0x", "1.5x", "2.0x"};
        final android.widget.TextView tvSpd = findViewById(R.id.tv_anim_speed_value);
        final android.view.View rowSpd = findViewById(R.id.row_anim_speed);
        if (tvSpd != null && rowSpd != null) {
            float curs = sp.getFloat("anim_speed", 1.0f);
            int ci = 2; for (int i = 0; i < spd.length; i++) if (Math.abs(spd[i] - curs) < 0.01f) ci = i;
            tvSpd.setText(spdNames[ci]);
            final int[] sel = { ci };
            rowSpd.setOnClickListener(x -> showStyledChoice(getString(R.string.anim_speed_title), spdNames, sel[0], idx -> {
                sel[0] = idx; sp.edit().putFloat("anim_speed", spd[idx]).apply(); tvSpd.setText(spdNames[idx]);
            }));
        }
    }
    private void showStyledChoice(CharSequence title, CharSequence[] names, int current, OnPick cb) {
        android.view.View v = android.view.LayoutInflater.from(this).inflate(R.layout.dialog_single_choice, null);
        ((android.widget.TextView) v.findViewById(R.id.dialog_choice_title)).setText(title);
        android.widget.RadioGroup rg = v.findViewById(R.id.dialog_choice_group);
        float d = getResources().getDisplayMetrics().density;
        for (int i = 0; i < names.length; i++) {
            android.widget.RadioButton rb = new android.widget.RadioButton(this);
            rb.setId(i + 1);
            rb.setText(names[i]);
            rb.setTextSize(17);
            rb.setTextColor(0xFF1A1A2E);
            rb.setPadding((int) (8 * d), 0, 0, 0);
            rb.setGravity(android.view.Gravity.CENTER_VERTICAL);
            rb.setButtonTintList(android.content.res.ColorStateList.valueOf(0xFF1976D2));
            rg.addView(rb, new android.widget.RadioGroup.LayoutParams(android.widget.RadioGroup.LayoutParams.MATCH_PARENT, (int) (60 * d)));
        }
        rg.check(current + 1);
        final androidx.appcompat.app.AlertDialog dialog = new androidx.appcompat.app.AlertDialog.Builder(this).setView(v).create();
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        rg.setOnCheckedChangeListener((g, id) -> { cb.pick(id - 1); dialog.dismiss(); });
        dialog.show();
    }
    private void applyCardAlpha(androidx.cardview.widget.CardView card) {
        if (card == null) return;
        int pref = getSharedPreferences("s", MODE_PRIVATE).getInt("setting_card_alpha", 0);
        card.setCardBackgroundColor(((255 - (int) (pref / 100f * 255)) << 24) | 0x00FFFFFF);
    }
    private void applyWallpaperBg(android.widget.ImageView wp) {
        if (wp == null) return;
        android.content.SharedPreferences prefs = getSharedPreferences("s", MODE_PRIVATE);
        int type = prefs.getInt("wallpaper_type", 4);
        wp.setAlpha(prefs.getFloat("wallpaper_alpha", 0.3f));
        if (type == 3) {
            android.graphics.Bitmap bm = WallpaperStore.loadPage(this, "settings");
            if (bm != null) { wp.setImageBitmap(bm); return; }
            wp.setImageDrawable(androidx.core.content.ContextCompat.getDrawable(this, R.drawable.wallpaper_white));
            return;
        }
        wp.setImageDrawable(WallpaperStore.presetDrawable(this, type));
    }
    @Override public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == android.R.id.home) { finish(); return true; }
        return super.onOptionsItemSelected(item);
    }
}
