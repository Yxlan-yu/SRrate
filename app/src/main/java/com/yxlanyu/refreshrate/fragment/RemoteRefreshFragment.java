package com.yxlanyu.refreshrate.fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.*;
import android.widget.*;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.adapter.RateAdapter;
import com.yxlanyu.refreshrate.adb.AdbConnectionManager;
import com.yxlanyu.refreshrate.model.DisplayMode;
import io.github.muntashirakon.adb.AbsAdbConnectionManager;
import io.github.muntashirakon.adb.AdbStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class RemoteRefreshFragment extends Fragment {
    private static final int GREEN = 0xFF2ECC71, AMBER = 0xFFFFB300, RED = 0xFFE57373;
    private SharedPreferences prefs;
    private boolean wirelessMode = true;
    private boolean connected = false;
    private AbsAdbConnectionManager manager;
    private final java.util.List<DisplayMode> remoteModes = new java.util.ArrayList<>();
    private EditText etIp, etPort, etPair, etPairPort;
    private TextView tvStatus, tvModesEmpty, tvHistoryTitle;
    private LinearLayout llWirelessInputs, llUsbHint, llHistory;
    private RecyclerView rvModes;
    private TextView btnWireless;
    private TextView btnUsb;
    private TextView tvDevice;
    private androidx.appcompat.widget.SwitchCompat swBlurIp;
    private LinearLayout llDevices;
    private static final int MAX_DEV = 3;
    private static final java.util.List<RemoteDev> HUB = new java.util.ArrayList<>();
    private static int activeIdx = -1;
    static class RemoteDev {
        String ip, port, model = "", dump = "", devInfo = "";
        AbsAdbConnectionManager mgr;
        RemoteDev(String ip, String port, AbsAdbConnectionManager mgr) { this.ip = ip; this.port = port; this.mgr = mgr; }
    }
    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup container, Bundle s) {
        View v = inf.inflate(R.layout.fragment_remote, container, false);
        prefs = requireActivity().getSharedPreferences("s", Context.MODE_PRIVATE);
        btnWireless = v.findViewById(R.id.btn_mode_wireless);
        btnUsb = v.findViewById(R.id.btn_mode_usb);
        btnWireless.setOnClickListener(x -> setMode(true));
        btnUsb.setOnClickListener(x -> setMode(false));
        llWirelessInputs = v.findViewById(R.id.ll_wireless_inputs);
        llUsbHint = v.findViewById(R.id.ll_usb_hint);
        llHistory = v.findViewById(R.id.ll_remote_history);
        tvHistoryTitle = v.findViewById(R.id.tv_history_title);
        etIp = v.findViewById(R.id.et_remote_ip);
        etPort = v.findViewById(R.id.et_remote_port);
        etPair = v.findViewById(R.id.et_remote_pair);
        etPairPort = v.findViewById(R.id.et_remote_pair_port);
        tvStatus = v.findViewById(R.id.tv_remote_status);
        tvModesEmpty = v.findViewById(R.id.tv_remote_modes_empty);
        rvModes = v.findViewById(R.id.recycler_remote_modes);
        rvModes.setLayoutManager(new LinearLayoutManager(getContext()));
        rvModes.setNestedScrollingEnabled(false);
        tvDevice = v.findViewById(R.id.tv_remote_device);
        swBlurIp = v.findViewById(R.id.switch_blur_ip);
        swBlurIp.setChecked(prefs.getBoolean("remote_blur_ip", false));
        swBlurIp.setOnCheckedChangeListener((b, on) -> {
            prefs.edit().putBoolean("remote_blur_ip", on).apply();
            applyIpMask();
        });
        etIp.setOnFocusChangeListener((vv, has) -> applyIpMask());
        applyIpMask();
        applyCardAlpha(v);
        llDevices = v.findViewById(R.id.ll_remote_devices);
        renderChips();
        if (activeIdx >= 0 && activeIdx < HUB.size()) {
            RemoteDev d = HUB.get(activeIdx);
            manager = d.mgr; connected = true;
            etIp.setText(d.ip); etPort.setText(d.port);
            applyIpMask();
            populateModes(d.dump); setDeviceInfo(d.devInfo);
            setStatus(connectedText(d.ip), GREEN);
        }
        setMode(true);
        refreshHistory();
        v.findViewById(R.id.btn_remote_pair).setOnClickListener(x -> doPair());
        v.findViewById(R.id.btn_remote_connect).setOnClickListener(x -> doConnect());
        return v;
    }
    @Override
    public void onResume() {
        super.onResume();
        applyIpMask();
        applyRemoteAlpha();
    }
    private void applyRemoteAlpha() {
        if (prefs == null) return;
        float cardA = 1f - (prefs.getInt("remote_card_alpha", 0) / 100f);
        float textA = 1f - (prefs.getInt("remote_text_alpha", 0) / 100f);
        View root = getView();
        if (root instanceof android.view.ViewGroup) applyAlphaRec((android.view.ViewGroup) root, cardA, textA);
    }
    private void applyAlphaRec(android.view.ViewGroup vg, float cardA, float textA) {
        for (int i = 0; i < vg.getChildCount(); i++) {
            android.view.View c = vg.getChildAt(i);
            if (c instanceof androidx.cardview.widget.CardView) {
                c.setAlpha(cardA);
            } else if (c instanceof android.widget.TextView) {
                c.setAlpha(textA);
            }
            if (c instanceof android.view.ViewGroup) applyAlphaRec((android.view.ViewGroup) c, cardA, textA);
        }
    }
    private void applyIpMask() {
        if (etIp == null) return;
        boolean mask = prefs.getBoolean("remote_blur_ip", false) && !etIp.hasFocus();
        etIp.setTransformationMethod(mask ? android.text.method.PasswordTransformationMethod.getInstance() : null);
    }
    private void setDeviceInfo(String raw) {
        if (tvDevice == null) return;
        if (raw == null) { tvDevice.setVisibility(View.GONE); return; }
        String[] L = raw.split("\n");
        String market = L.length > 0 ? L[0].trim() : "";
        String oplus = L.length > 1 ? L[1].trim() : "";
        String model = L.length > 2 ? L[2].trim() : "";
        String ver = L.length > 3 ? L[3].trim() : "";
        String name = !market.isEmpty() ? market : (!oplus.isEmpty() ? oplus : model);
        if (name.isEmpty()) { tvDevice.setVisibility(View.GONE); return; }
        String txt = name;
        if (!ver.isEmpty()) txt += "  ·  Android " + ver;
        tvDevice.setText(txt);
        tvDevice.setVisibility(View.VISIBLE);
    }
    private int findDev(String ip, String port) {
        for (int i = 0; i < HUB.size(); i++)
            if (HUB.get(i).ip.equals(ip) && HUB.get(i).port.equals(port)) return i;
        return -1;
    }
    private static String parseName(String raw) {
        if (raw == null) return "";
        String[] L = raw.split("\n");
        String market = L.length > 0 ? L[0].trim() : "";
        String oplus = L.length > 1 ? L[1].trim() : "";
        String model = L.length > 2 ? L[2].trim() : "";
        return !market.isEmpty() ? market : (!oplus.isEmpty() ? oplus : model);
    }
    private void selectDevice(int idx) {
        if (idx < 0 || idx >= HUB.size()) return;
        activeIdx = idx;
        RemoteDev d = HUB.get(idx);
        manager = d.mgr; connected = true;
        etIp.setText(d.ip); etPort.setText(d.port);
        applyIpMask();
        populateModes(d.dump);
        setDeviceInfo(d.devInfo);
        setStatus(connectedText(d.ip), GREEN);
        renderChips();
    }
    private void disconnectDevice(int idx) {
        if (idx < 0 || idx >= HUB.size()) return;
        try { HUB.get(idx).mgr.getClass().getMethod("close").invoke(HUB.get(idx).mgr); } catch (Throwable ig) {}
        HUB.remove(idx);
        if (HUB.isEmpty()) {
            activeIdx = -1; manager = null; connected = false;
            showEmpty(R.string.remote_status_none);
            if (tvDevice != null) tvDevice.setVisibility(View.GONE);
            setStatus(getString(R.string.remote_status_idle), AMBER);
            try { com.yxlanyu.refreshrate.service.RemoteKeepAliveService.stop(requireContext().getApplicationContext()); } catch (Throwable ig) {}
            renderChips();
        } else {
            selectDevice(0);
        }
    }
    private void renderChips() {
        if (llDevices == null) return;
        llDevices.removeAllViews();
        if (HUB.isEmpty()) { llDevices.setVisibility(View.GONE); return; }
        llDevices.setVisibility(View.VISIBLE);
        for (int i = 0; i < HUB.size(); i++) {
            final int idx = i;
            RemoteDev d = HUB.get(i);
            boolean active = (i == activeIdx);
            LinearLayout chip = new LinearLayout(getContext());
            chip.setOrientation(LinearLayout.HORIZONTAL);
            chip.setGravity(android.view.Gravity.CENTER_VERTICAL);
            chip.setBackgroundColor(active ? 0xFF1976D2 : 0x1A000000);
            chip.setPadding(dp(12), dp(7), dp(8), dp(7));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, dp(8), 0);
            chip.setLayoutParams(lp);
            TextView label = new TextView(getContext());
            label.setText(d.model.isEmpty() ? d.ip : d.model);
            label.setTextSize(13);
            label.setTextColor(active ? 0xFFFFFFFF : 0xFF555555);
            label.setOnClickListener(x -> selectDevice(idx));
            chip.addView(label);
            TextView del = new TextView(getContext());
            del.setText("✕");
            del.setTextSize(13);
            del.setTextColor(active ? 0xFFFFFFFF : 0xFF888888);
            del.setPadding(dp(9), 0, dp(2), 0);
            del.setOnClickListener(x -> disconnectDevice(idx));
            chip.addView(del);
            llDevices.addView(chip);
        }
    }
    private void setMode(boolean wireless) {
        wirelessMode = wireless;
        llWirelessInputs.setVisibility(wireless ? View.VISIBLE : View.GONE);
        llUsbHint.setVisibility(wireless ? View.GONE : View.VISIBLE);
        styleTab(btnWireless, wireless);
        styleTab(btnUsb, !wireless);
        View vv = getView();
        if (vv != null) {
            View modesCard = vv.findViewById(R.id.card_remote_modes);
            if (modesCard != null) modesCard.setVisibility(wireless ? View.VISIBLE : View.GONE);
            if (tvStatus != null) tvStatus.setVisibility(wireless ? View.VISIBLE : View.GONE);
        }
    }
    private void styleTab(TextView tab, boolean selected) {
        if (tab == null) return;
        tab.setBackgroundResource(selected ? R.drawable.bg_tab_selected : R.drawable.bg_tab_unselected);
        tab.setTextColor(selected ? 0xFFFFFFFF : 0xFF546E7A);
    }
    private String connectedText(String ip) {
        if (prefs.getBoolean("remote_blur_ip", false))
            return getString(R.string.remote_connected, getString(R.string.remote_wireless_device));
        return getString(R.string.remote_connected, ip);
    }
    private void applyCardAlpha(View v) {
        int pref = prefs.getInt("setting_card_alpha", 0);
        int a = 255 - (int) (pref / 100f * 255);
        if (a < 0) a = 0;
        if (a > 255) a = 255;
        int color = (a << 24) | 0x00FFFFFF;
        int[] ids = { R.id.card_remote_conn, R.id.card_remote_modes };
        for (int id : ids) {
            View card = v.findViewById(id);
            if (card instanceof CardView) ((CardView) card).setCardBackgroundColor(color);
        }
    }
    private void doPair() {
        if (!wirelessMode) return;
        final String ip = etIp.getText().toString().trim();
        final String port = etPairPort.getText().toString().trim();
        final String code = etPair.getText().toString().trim();
        if (ip.isEmpty() || port.isEmpty() || code.isEmpty()) {
            setStatus(getString(R.string.remote_need_pair_input), RED);
            return;
        }
        setStatus(getString(R.string.remote_pairing), AMBER);
        new Thread(() -> {
            boolean ok = false;
            String err = null;
            try {
                AbsAdbConnectionManager m = AdbConnectionManager.newInstance(requireContext());
                ok = m.pair(ip, Integer.parseInt(port), code);
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final boolean fok = ok;
            final String ferr = err;
            post(() -> {
                if (fok) setStatus(getString(R.string.remote_pair_ok), GREEN);
                else setStatus(getString(R.string.remote_pair_fail, ferr), RED);
            });
        }).start();
    }
    private void doConnect() {
        if (!wirelessMode) return;
        final String ip = etIp.getText().toString().trim();
        final String port = etPort.getText().toString().trim();
        if (ip.isEmpty() || port.isEmpty()) {
            setStatus(getString(R.string.remote_status_need_input), RED);
            return;
        }
        setStatus(getString(R.string.remote_conn_wait), AMBER);
        new Thread(() -> {
            boolean ok = false;
            String err = null;
            String dump = null;
            String devInfo = null;
            AbsAdbConnectionManager m = null;
            try {
                m = AdbConnectionManager.newInstance(requireContext());
                ok = m.connect(ip, Integer.parseInt(port));
                if (ok) {
                    dump = exec(m, "dumpsys display");
                    devInfo = exec(m, "getprop ro.product.marketname; getprop ro.vendor.oplus.market.name; getprop ro.product.model; getprop ro.build.version.release");
                }
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final boolean fok = ok;
            final String ferr = err;
            final String fdump = dump;
            final String fdevInfo = devInfo;
            final AbsAdbConnectionManager fm = m;
            post(() -> {
                if (fok) {
                    int slot = findDev(ip, port);
                    if (slot < 0 && HUB.size() >= MAX_DEV) {
                        setStatus(getString(R.string.remote_max_devices), AMBER);
                        return;
                    }
                    RemoteDev d;
                    if (slot >= 0) {
                        d = HUB.get(slot);
                        try { d.mgr.getClass().getMethod("close").invoke(d.mgr); } catch (Throwable ig) {}
                        d.mgr = fm; activeIdx = slot;
                    } else {
                        d = new RemoteDev(ip, port, fm); HUB.add(d); activeIdx = HUB.size() - 1;
                    }
                    d.dump = fdump == null ? "" : fdump;
                    d.devInfo = fdevInfo == null ? "" : fdevInfo;
                    d.model = parseName(d.devInfo);
                    manager = fm; connected = true;
                    saveHistory(ip, port);
                    setStatus(connectedText(ip), GREEN);
                    populateModes(d.dump);
                    setDeviceInfo(d.devInfo);
                    renderChips();
                    refreshHistory();
                    com.yxlanyu.refreshrate.service.RemoteKeepAliveService.start(requireContext().getApplicationContext());
                } else {
                    setStatus(getString(R.string.remote_conn_fail, ferr), RED);
                }
            });
        }).start();
    }
    private static String exec(AbsAdbConnectionManager m, String cmd) throws Exception {
        AdbStream stream = m.openStream("shell:" + cmd);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            InputStream is = stream.openInputStream();
            byte[] buf = new byte[4096];
            int n;
            while ((n = is.read(buf)) > 0) bos.write(buf, 0, n);
        } catch (Exception readEnd) {
        } finally {
            try { stream.close(); } catch (Exception ignored) {}
        }
        return new String(bos.toByteArray());
    }
    private void populateModes(String dump) {
        if (dump == null) {
            showEmpty(R.string.remote_modes_parse_fail);
            return;
        }
        Pattern p = Pattern.compile("id=(\\d+),\\s*width=(\\d+),\\s*height=(\\d+),\\s*fps=([0-9.]+)");
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        LinkedHashMap<String, List<DisplayMode>> groups = new LinkedHashMap<>();
        int sfIdx = 0;
        boolean usedRecords = false;
        for (String line : dump.split("\n")) {
            if (!line.contains("DisplayModeRecord")) continue;
            Matcher m = p.matcher(line);
            if (m.find()) {
                usedRecords = true;
                addMode(groups, seen, m, sfIdx);
                sfIdx++;
            }
        }
        if (!usedRecords) {
            Matcher m = p.matcher(dump);
            while (m.find()) addMode(groups, seen, m, -1);
        }
        if (groups.isEmpty()) {
            showEmpty(R.string.remote_modes_parse_fail);
            return;
        }
        List<Object> items = new ArrayList<>();
        for (Map.Entry<String, List<DisplayMode>> en : groups.entrySet()) {
            List<DisplayMode> g = en.getValue();
            Collections.sort(g, (a, b) -> b.getRateInt() - a.getRateInt());
            items.add(en.getKey());
            items.addAll(g);
        }
        remoteModes.clear();
        for (List<DisplayMode> g : groups.values()) remoteModes.addAll(g);
        tvModesEmpty.setVisibility(View.GONE);
        rvModes.setVisibility(View.VISIBLE);
        RateAdapter ad = new RateAdapter(getContext(), items,
                m -> applyRemoteMode(m.getWidth(), m.getHeight(), m.getRateInt(), m.getSfIndex()));
        rvModes.setAdapter(ad);
    }
    private static void addMode(LinkedHashMap<String, List<DisplayMode>> groups, LinkedHashSet<String> seen, Matcher m, int sfIndex) {
        int id = Integer.parseInt(m.group(1));
        int w = Integer.parseInt(m.group(2));
        int h = Integer.parseInt(m.group(3));
        float fps = Float.parseFloat(m.group(4));
        String key = w + "x" + h + "@" + Math.round(fps);
        if (!seen.add(key)) return;
        DisplayMode dm = new DisplayMode(w, h, fps, id);
        if (sfIndex >= 0) dm.setSfIndex(sfIndex);
        String res = w + " × " + h;
        List<DisplayMode> g = groups.get(res);
        if (g == null) {
            g = new ArrayList<>();
            groups.put(res, g);
        }
        g.add(dm);
    }
    private void showEmpty(int res) {
        if (rvModes != null) rvModes.setVisibility(View.GONE);
        tvModesEmpty.setVisibility(View.VISIBLE);
        tvModesEmpty.setText(res);
    }
    private void applyRemoteMode(int w, int h, int rr, int sfIndex) {
        if (!connected || manager == null) return;
        setStatus(getString(R.string.remote_switching, rr), AMBER);
        new Thread(() -> {
            int active = -1;
            String err = null;
            try {
                String setCmd = "cmd display set-user-preferred-display-mode " + w + " " + h + " " + rr + " 2>/dev/null; "
                        + "settings put system peak_refresh_rate " + rr + ".0; "
                        + "settings put system min_refresh_rate " + rr + ".0; "
                        + "settings put system user_refresh_rate " + rr + "; "
                        + "settings put secure miui_refresh_rate " + rr + "; "
                        + "settings put system thermal_limit_refresh_rate " + rr + " 2>/dev/null";
                if (sfIndex >= 0) setCmd += "; service call SurfaceFlinger 1035 i32 " + sfIndex;
                exec(manager, setCmd);
                Thread.sleep(600);
                active = parseActiveFps(exec(manager, "dumpsys display"));
            } catch (Throwable t) {
                err = String.valueOf(t.getMessage());
            }
            final int fa = active;
            final String ferr = err;
            post(() -> {
                if (ferr != null) setStatus(getString(R.string.remote_switch_fail, ferr), RED);
                else if (fa <= 0) setStatus(getString(R.string.remote_switch_sent), AMBER);
                else if (fa == rr) setStatus(getString(R.string.remote_switch_ok, rr), GREEN);
                else setStatus(getString(R.string.remote_switch_actual, fa), AMBER);
            });
        }).start();
    }
    private static int parseActiveFps(String dump) {
        if (dump == null) return -1;
        try {
            Matcher a = Pattern.compile("mActiveModeId=(\\d+)").matcher(dump);
            if (a.find()) {
                int id = Integer.parseInt(a.group(1));
                Matcher f = Pattern.compile("id=" + id + ", width=\\d+, height=\\d+, fps=([0-9.]+)").matcher(dump);
                if (f.find()) return Math.round(Float.parseFloat(f.group(1)));
            }
        } catch (Exception e) {}
        try {
            Matcher r = Pattern.compile("refreshRate=?\\s?([0-9]+(?:\\.[0-9]+)?)").matcher(dump);
            if (r.find()) return Math.round(Float.parseFloat(r.group(1)));
        } catch (Exception e) {}
        return -1;
    }
    private void saveHistory(String ip, String port) {
        String entry = ip + "," + port;
        String raw = prefs.getString("remote_history", "");
        ArrayList<String> list = new ArrayList<>();
        list.add(entry);
        if (!raw.isEmpty()) {
            for (String e : raw.split(";")) {
                if (!e.isEmpty() && !e.equals(entry) && list.size() < 6) list.add(e);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(list.get(i));
        }
        prefs.edit().putString("remote_history", sb.toString()).apply();
    }
    private void refreshHistory() {
        String raw = prefs.getString("remote_history", "");
        llHistory.removeAllViews();
        if (raw.isEmpty()) {
            tvHistoryTitle.setVisibility(View.GONE);
            llHistory.setVisibility(View.GONE);
            return;
        }
        tvHistoryTitle.setVisibility(View.VISIBLE);
        llHistory.setVisibility(View.VISIBLE);
        for (String e : raw.split(";")) {
            if (e.isEmpty()) continue;
            String[] parts = e.split(",");
            if (parts.length < 2) continue;
            final String ip = parts[0];
            final String port = parts[1];
            final String entry = e;
            LinearLayout row = new LinearLayout(getContext());
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(android.view.Gravity.CENTER_VERTICAL);
            row.setBackgroundColor(0x0D1976D2);
            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            rlp.topMargin = dp(6);
            row.setLayoutParams(rlp);
            TextView txt = new TextView(getContext());
            txt.setText(ip + " : " + port);
            txt.setTextSize(14);
            txt.setTextColor(0xFF1976D2);
            txt.setPadding(dp(12), dp(11), dp(8), dp(11));
            txt.setLayoutParams(new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
            txt.setOnClickListener(x -> {
                etIp.setText(ip);
                etPort.setText(port);
                applyIpMask();
            });
            TextView del = new TextView(getContext());
            del.setText("✕");
            del.setTextSize(15);
            del.setTextColor(0xFFE57373);
            del.setPadding(dp(14), dp(11), dp(16), dp(11));
            del.setOnClickListener(x -> {
                deleteHistory(entry);
                refreshHistory();
            });
            row.addView(txt);
            row.addView(del);
            llHistory.addView(row);
        }
    }
    private void deleteHistory(String entry) {
        String raw = prefs.getString("remote_history", "");
        ArrayList<String> list = new ArrayList<>();
        for (String e : raw.split(";")) {
            if (!e.isEmpty() && !e.equals(entry)) list.add(e);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(";");
            sb.append(list.get(i));
        }
        prefs.edit().putString("remote_history", sb.toString()).apply();
    }
    private int dp(int val) {
        return (int) (val * getResources().getDisplayMetrics().density);
    }
    private void post(Runnable r) {
        if (isAdded() && getActivity() != null) getActivity().runOnUiThread(r);
    }
    private void setStatus(String s, int color) {
        if (tvStatus != null) {
            tvStatus.setText(s);
            tvStatus.setTextColor(color);
        }
    }
}
