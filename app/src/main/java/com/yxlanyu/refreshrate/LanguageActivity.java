package com.yxlanyu.refreshrate;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import com.yxlanyu.refreshrate.util.LanguageUtils;
public class LanguageActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private final String[] codes = {
        LanguageUtils.LANG_SYSTEM, LanguageUtils.LANG_ZH, LanguageUtils.LANG_ZH_TW,
        LanguageUtils.LANG_EN, LanguageUtils.LANG_JA
    };
    private final int[] nameRes = {
        R.string.lang_system, R.string.lang_zh, R.string.lang_zh_tw,
        R.string.lang_en, R.string.lang_ja
    };
    private final String[] badges = {"文A", "简", "繁", "EN", "JA"};
    @Override protected void onCreate(Bundle s) {
        LanguageUtils.applyLanguage(this);
        super.onCreate(s);
        int pad = dp(16);
        ScrollView sv = new ScrollView(this);
        sv.setBackgroundColor(0x00000000);
        sv.setFillViewport(true);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, dp(48), pad, pad);
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView back = new TextView(this);
        back.setText("\u2190");
        back.setTextSize(24);
        back.setTextColor(0xFF222222);
        back.setPadding(0, 0, dp(16), 0);
        back.setOnClickListener(v -> finish());
        TextView title = new TextView(this);
        title.setText(R.string.language_page_title);
        title.setTextSize(24);
        title.setTextColor(0xFF222222);
        title.getPaint().setFakeBoldText(true);
        header.addView(back);
        header.addView(title);
        root.addView(header);
        root.addView(space(dp(20)));
        String cur = LanguageUtils.getCurrentLang(this);
        for (int i = 0; i < codes.length; i++) {
            root.addView(buildRow(i, codes[i].equals(cur)));
            root.addView(space(dp(12)));
        }
        sv.addView(root);
        android.widget.FrameLayout container = new android.widget.FrameLayout(this);
        android.widget.ImageView wp = new android.widget.ImageView(this);
        wp.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
        wp.setLayoutParams(new android.widget.FrameLayout.LayoutParams(
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT,
                android.widget.FrameLayout.LayoutParams.MATCH_PARENT));
        applyWallpaperBg(wp);
        container.addView(wp);
        container.addView(sv);
        setContentView(container);
    }
    private void applyWallpaperBg(android.widget.ImageView wp) {
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
    private View buildRow(final int idx, boolean selected) {
        CardView card = new CardView(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, dp(64));
        card.setLayoutParams(lp);
        card.setRadius(dp(18));
        card.setCardElevation(0f);
        int baseColor = selected ? 0xFFD9D7F5 : 0xFFE9E8F2;
        int cardAlphaPref = getSharedPreferences("s", MODE_PRIVATE).getInt("setting_card_alpha", 0);
        int a = 255 - (int) (cardAlphaPref / 100f * 255);
        if (a < 0) a = 0; if (a > 255) a = 255;
        card.setCardBackgroundColor((a << 24) | (baseColor & 0x00FFFFFF));
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(18), 0, dp(18), 0);
        TextView badge = new TextView(this);
        badge.setText(badges[idx]);
        badge.setTextColor(0xFF555555);
        badge.setTextSize(13);
        badge.setGravity(Gravity.CENTER);
        android.graphics.drawable.GradientDrawable circle = new android.graphics.drawable.GradientDrawable();
        circle.setShape(android.graphics.drawable.GradientDrawable.OVAL);
        circle.setColor(0xFFD8D7E2);
        badge.setBackground(circle);
        LinearLayout.LayoutParams bl = new LinearLayout.LayoutParams(dp(40), dp(40));
        bl.rightMargin = dp(16);
        badge.setLayoutParams(bl);
        TextView name = new TextView(this);
        name.setText(nameRes[idx]);
        name.setTextSize(18);
        name.setTextColor(0xFF222222);
        row.addView(badge);
        row.addView(name);
        card.addView(row);
        card.setOnClickListener(v -> {
            LanguageUtils.setLanguageAndRecreate(LanguageActivity.this, codes[idx]);
        });
        return card;
    }
    private View space(int h) {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, h));
        return v;
    }
    private int dp(float d) { return (int) (d * getResources().getDisplayMetrics().density); }
}
