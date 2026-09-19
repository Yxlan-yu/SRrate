package com.yxlanyu.refreshrate;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.adapter.AppListAdapter;
import com.yxlanyu.refreshrate.model.AppInfo;
import com.yxlanyu.refreshrate.util.LanguageUtils;
import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class AppListActivity extends AppCompatActivity {
    @Override protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(com.yxlanyu.refreshrate.util.LanguageUtils.wrap(newBase));
    }
    private SharedPreferences prefs;
    private final List<AppInfo> allApps = new ArrayList<>();
    private AppListAdapter adapter;
    private EditText etSearch;
    private TextView tvHint;
    private TextView tvEmpty;
    private boolean showSystemApps = false;
    @Override protected void onCreate(Bundle s) {
        super.onCreate(s);
        LanguageUtils.applyLanguage(this);
        setContentView(R.layout.activity_app_list);
        prefs = getSharedPreferences("s", MODE_PRIVATE);
        showSystemApps = prefs.getBoolean("show_system_apps_in_list", false);
        ImageView btnBack = findViewById(R.id.btn_back);
        ImageView btnSearch = findViewById(R.id.btn_search);
        ImageView btnMore = findViewById(R.id.btn_more);
        etSearch = findViewById(R.id.et_search);
        tvHint = findViewById(R.id.tv_app_list_hint);
        tvEmpty = findViewById(R.id.tv_empty);
        btnBack.setOnClickListener(v -> finish());
        btnSearch.setOnClickListener(v -> {
            etSearch.setVisibility(etSearch.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
            if (etSearch.getVisibility() == View.VISIBLE) etSearch.requestFocus();
        });
        btnMore.setOnClickListener(this::showMoreMenu);
        RecyclerView rv = findViewById(R.id.recycler_apps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AppListAdapter(this, app -> {
            Intent i = new Intent(this, AppRefreshConfigActivity.class);
            i.putExtra("pkg", app.getEffectivePkg());
            startActivity(i);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });
        rv.setAdapter(adapter);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterApps(); }
            @Override public void afterTextChanged(Editable s) {}
        });
        showAppListPermissionHint();
        loadApps();
    }
    private void showAppListPermissionHint() {
        if (prefs.getBoolean("app_list_permission_hint_shown", false)) return;
        prefs.edit().putBoolean("app_list_permission_hint_shown", true).apply();
        new AlertDialog.Builder(this)
                .setTitle(R.string.app_list_permission_title)
                .setMessage(R.string.app_list_permission_message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
    private void showMoreMenu(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        android.view.MenuItem item = popup.getMenu().add(R.string.show_system_apps);
        item.setCheckable(true);
        item.setChecked(showSystemApps);
        popup.setOnMenuItemClickListener(menuItem -> {
            showSystemApps = !showSystemApps;
            prefs.edit().putBoolean("show_system_apps_in_list", showSystemApps).apply();
            updateHint();
            filterApps();
            return true;
        });
        popup.show();
    }
    private void loadApps() {
        new Thread(() -> {
            List<AppInfo> result = new ArrayList<>();
            PackageManager pm = getPackageManager();
            try {
                List<ApplicationInfo> installed = pm.getInstalledApplications(PackageManager.GET_META_DATA);
                java.util.Set<String> primaryPkgs = new java.util.HashSet<>();
                for (ApplicationInfo ai : installed) {
                    String pkg = ai.packageName;
                    if (pkg == null || pkg.equals(getPackageName())) continue;
                    primaryPkgs.add(pkg);
                    boolean system = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
                    String label = String.valueOf(pm.getApplicationLabel(ai));
                    result.add(new AppInfo(label, pkg, pm.getApplicationIcon(ai), system, 0));
                }
                try {
                    android.os.UserManager um = (android.os.UserManager) getSystemService(android.content.Context.USER_SERVICE);
                    List<android.os.UserHandle> profiles = um.getUserProfiles();
                    for (android.os.UserHandle uh : profiles) {
                        int uid = 0;
                        try {
                            java.lang.reflect.Method getId = android.os.UserHandle.class.getDeclaredMethod("getIdentifier");
                            uid = (int) getId.invoke(uh);
                        } catch (Exception ignored) {}
                        if (uid == 0) continue;
                        try {
                            java.lang.reflect.Method getAppsAsUser = PackageManager.class.getMethod(
                                "getInstalledApplicationsAsUser", int.class, int.class);
                            @SuppressWarnings("unchecked")
                            List<ApplicationInfo> cloneApps = (List<ApplicationInfo>) getAppsAsUser.invoke(pm, PackageManager.GET_META_DATA, uid);
                            if (cloneApps == null) continue;
                            for (ApplicationInfo ai : cloneApps) {
                                String pkg = ai.packageName;
                                if (pkg == null || !primaryPkgs.contains(pkg)) continue;
                                if (pkg.equals(getPackageName())) continue;
                                boolean system = (ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0 && (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0;
                                String label = String.valueOf(pm.getApplicationLabel(ai)) + " (分身)";
                                result.add(new AppInfo(label, pkg, pm.getApplicationIcon(ai), system, uid));
                            }
                        } catch (Exception ignored) {}
                    }
                } catch (Exception ignored) {}
                Collator collator = Collator.getInstance(Locale.getDefault());
                result.sort((a, b) -> {
                    int c = collator.compare(a.getPackageName(), b.getPackageName());
                    return c != 0 ? c : Integer.compare(a.getUserId(), b.getUserId());
                });
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.app_list_load_failed, Toast.LENGTH_SHORT).show());
            }
            runOnUiThread(() -> {
                allApps.clear();
                allApps.addAll(result);
                updateHint();
                filterApps();
            });
        }).start();
    }
    private void updateHint() {
        if (tvHint == null) return;
        tvHint.setText(showSystemApps ? R.string.app_list_all_hint : R.string.app_list_third_party_hint);
    }
    private void filterApps() {
        String q = etSearch == null ? "" : etSearch.getText().toString().trim().toLowerCase(Locale.ROOT);
        List<AppInfo> shown = new ArrayList<>();
        for (AppInfo app : allApps) {
            if (!showSystemApps && app.isSystemApp()) continue;
            if (!q.isEmpty()) {
                String n = app.getName().toLowerCase(Locale.ROOT);
                String p = app.getPackageName().toLowerCase(Locale.ROOT);
                if (!n.contains(q) && !p.contains(q)) continue;
            }
            shown.add(app);
        }
        adapter.submit(shown);
        if (tvEmpty != null) tvEmpty.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
    @Override public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
