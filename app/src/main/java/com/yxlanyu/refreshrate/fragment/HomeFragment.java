package com.yxlanyu.refreshrate.fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.*;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.adapter.RateAdapter;
import com.yxlanyu.refreshrate.model.DisplayMode;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import com.yxlanyu.refreshrate.util.RootUtils;
import java.util.*;
public class HomeFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    private RateAdapter adapter;
    private boolean hasRoot = false;
    private List<Object> dataItems = new ArrayList<>();
    private RecyclerView recyclerView;
    private SharedPreferences prefs;
    private View titleBar;
    private TextView titleText;
    private ImageView btnLanguage;
    private PopupWindow languagePopup;
    @Override public View onCreateView(LayoutInflater inf, ViewGroup c, Bundle s) {
        View v = inf.inflate(R.layout.fragment_home, c, false);
        recyclerView = v.findViewById(R.id.recycler_rates);
        titleBar = v.findViewById(R.id.home_title_bar);
        titleText = v.findViewById(R.id.home_title_text);
        titleText.setText(R.string.home_title);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setItemAnimator(null);
        adapter = new RateAdapter(getContext(), dataItems, this::onRateSelected);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView rv, int dx, int dy) {
                if (!(getActivity() instanceof com.yxlanyu.refreshrate.MainActivity)) return;
                com.yxlanyu.refreshrate.MainActivity ma = (com.yxlanyu.refreshrate.MainActivity) getActivity();
                if (dy > 8) ma.hideBottomNav();
                else if (dy < -8) ma.showBottomNav();
            }
        });
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        prefs.registerOnSharedPreferenceChangeListener(this);
        updateUI();
        loadData();
        return v;
    }
    private int[] getCurrentWHRate() {
        try {
            android.hardware.display.DisplayManager dm = (android.hardware.display.DisplayManager)
                    requireActivity().getSystemService(Context.DISPLAY_SERVICE);
            android.view.Display d = dm.getDisplay(android.view.Display.DEFAULT_DISPLAY);
            android.view.Display.Mode m = d.getMode();
            return new int[]{ m.getPhysicalWidth(), m.getPhysicalHeight(), Math.round(d.getRefreshRate()) };
        } catch (Exception e) { return new int[]{0, 0, 0}; }
    }
    private void showLanguagePopup() {
        if (languagePopup != null && languagePopup.isShowing()) {
            languagePopup.dismiss();
        }
        String[] languages = {
            getString(R.string.language_zh),
            getString(R.string.language_zh_tw),
            getString(R.string.language_en),
            getString(R.string.language_ja)
        };
        String[] langCodes = {LanguageUtils.LANG_ZH, LanguageUtils.LANG_ZH_TW, LanguageUtils.LANG_EN, LanguageUtils.LANG_JA};
        String currentLang = LanguageUtils.getCurrentLang(getContext());
        ListView listView = new ListView(getContext());
        ArrayAdapter<String> listAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_single_choice, languages);
        listView.setAdapter(listAdapter);
        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        for (int i = 0; i < langCodes.length; i++) {
            if (langCodes[i].equals(currentLang)) {
                listView.setItemChecked(i, true);
                break;
            }
        }
        listView.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLang = langCodes[position];
            if (!selectedLang.equals(LanguageUtils.getCurrentLang(getContext()))) {
                LanguageUtils.setLanguageAndRecreate(requireActivity(), selectedLang);
            }
            languagePopup.dismiss();
        });
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.RECTANGLE);
        bg.setCornerRadius(dpToPx(16));
        bg.setColor(Color.WHITE);
        listView.setBackground(bg);
        listView.setClipToOutline(true);
        int width = (int) (150 * getResources().getDisplayMetrics().density);
        languagePopup = new PopupWindow(listView, width, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        languagePopup.setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(Color.TRANSPARENT));
        languagePopup.setElevation(12);
        languagePopup.showAsDropDown(btnLanguage, 0, 8);
    }
    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }
    private void updateUI() {
        float wallpaperAlpha = prefs.getFloat("wallpaper_alpha", 0.3f);
        float titleAlpha = 1f - wallpaperAlpha;
        titleBar.setAlpha(titleAlpha);
        titleText.setAlpha(titleAlpha);
        int cardAlpha = prefs.getInt("card_alpha", 0);
        float cardAlphaF = 1f - (cardAlpha / 100f);
        recyclerView.setAlpha(cardAlphaF);
    }
    @Override public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("wallpaper_alpha".equals(key) || "card_alpha".equals(key) || "home_card_alpha".equals(key)) {
            updateUI();
        }
    }
    @Override public void onDestroyView() {
        super.onDestroyView();
        prefs.unregisterOnSharedPreferenceChangeListener(this);
        if (languagePopup != null) {
            languagePopup.dismiss();
            languagePopup = null;
        }
    }
    private void loadData() {
        new Thread(() -> {
            hasRoot = RootUtils.isRooted();
            List<DisplayMode> modes = null;
            if (hasRoot) {
                modes = RootUtils.getDisplayModesFromDumpsys();
                if (modes.isEmpty()) hasRoot = false;
            }
            List<Object> built = buildSortedList(modes);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dataItems.clear();
                    dataItems.addAll(built);
                    adapter.notifyDataSetChanged();
                    int selW = prefs.getInt("last_sel_w", 0);
                    int selH = prefs.getInt("last_sel_h", 0);
                    int selHz = prefs.getInt("last_sel_hz", 0);
                    if (selHz > 0) {
                        adapter.setCurrentByWHR(selW, selH, selHz);
                    } else {
                        int[] cur = getCurrentWHRate();
                        if (cur[2] > 0) adapter.setCurrentByWHR(cur[0], cur[1], cur[2]);
                    }
                });
            }
        }).start();
    }
    private void onRateSelected(DisplayMode mode) {
        String authMode = prefs.getString("auth_mode", "");
        boolean useRoot    = "root".equals(authMode) && hasRoot;
        boolean useShizuku = "shizuku".equals(authMode)
                && com.yxlanyu.refreshrate.util.ShizukuUtils.isAvailable()
                && com.yxlanyu.refreshrate.util.ShizukuUtils.hasPermission();
        if (!useRoot && !useShizuku) {
            Toast.makeText(getContext(), R.string.no_root_toast, Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(() -> {
            boolean ok = false;
            if (useRoot) {
                ok = RootUtils.setDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRateInt(), mode.getSfIndex());
            } else if (useShizuku) {
                ok = com.yxlanyu.refreshrate.util.ShizukuUtils.setDisplayMode(mode.getWidth(), mode.getHeight(), mode.getRateInt(), mode.getSfIndex());
            }
            final boolean success = ok;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (success) {
                        adapter.setCurrent(mode);
                        prefs.edit()
                            .putInt("last_sel_w", mode.getWidth())
                            .putInt("last_sel_h", mode.getHeight())
                            .putInt("last_sel_hz", mode.getRateInt())
                            .putInt("last_sel_sf", mode.getSfIndex())
                            .apply();
                        Toast.makeText(getContext(), getString(R.string.switch_success, mode.getRateInt()), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), R.string.switch_fail, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).start();
    }
    private List<DisplayMode> getFallbackFromDisplayManager() {
        List<DisplayMode> list = new ArrayList<>();
        try {
            android.hardware.display.DisplayManager dm = (android.hardware.display.DisplayManager)
                    requireActivity().getSystemService(Context.DISPLAY_SERVICE);
            android.view.Display d = dm.getDisplay(android.view.Display.DEFAULT_DISPLAY);
            android.view.Display.Mode[] modes = d.getSupportedModes();
            int sfIdx = 0;
            for (android.view.Display.Mode m : modes) {
                DisplayMode dmode = new DisplayMode(m.getPhysicalWidth(), m.getPhysicalHeight(),
                        m.getRefreshRate(), m.getModeId());
                dmode.setSfIndex(sfIdx++);
                list.add(dmode);
            }
        } catch (Exception ignored) {}
        return list;
    }
    private List<Object> buildSortedList(List<DisplayMode> dumpedModes) {
        if (dumpedModes == null || dumpedModes.isEmpty()) return fallback();
        Map<String, List<DisplayMode>> grouped = new LinkedHashMap<>();
        for (DisplayMode m : dumpedModes) {
            String k = m.getWidth() + "x" + m.getHeight();
            grouped.computeIfAbsent(k, x -> new ArrayList<>()).add(m);
        }
        List<String> keys = new ArrayList<>(grouped.keySet());
        keys.sort((a, b) -> {
            String[] pa = a.split("x"), pb = b.split("x");
            int resA = Integer.parseInt(pa[0]) * Integer.parseInt(pa[1]);
            int resB = Integer.parseInt(pb[0]) * Integer.parseInt(pb[1]);
            return Integer.compare(resB, resA);
        });
        List<Object> items = new ArrayList<>();
        Context ctx = getContext();
        for (int i = 0; i < keys.size(); i++) {
            List<DisplayMode> ms = grouped.get(keys.get(i));
            ms.sort((a, b) -> Float.compare(b.getRefreshRate(), a.getRefreshRate()));
            String label = (i == 0) ?
                ctx.getString(R.string.high_resolution) + keys.get(i)
                                    : ctx.getString(R.string.low_resolution) + keys.get(i);
            items.add(label);
            items.addAll(ms);
        }
        return items;
    }
    private List<Object> fallback() {
        int[] highResRates = {60, 90, 120, 144, 165, 170, 175, 177};
        int[] lowResRates  = {60, 90, 120, 144, 165, 170, 175, 177};
        List<Object> l = new ArrayList<>();
        l.add(getString(R.string.fallback_high_res));
        for (int r : highResRates) l.add(new DisplayMode(1272, 2772, r, -1));
        l.add(getString(R.string.fallback_low_res));
        for (int r : lowResRates) l.add(new DisplayMode(1080, 2354, r, -1));
        return l;
    }
}
