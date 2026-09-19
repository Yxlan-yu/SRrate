package com.yxlanyu.refreshrate.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.AppInfo;
import java.util.ArrayList;
import java.util.List;
public class AppListAdapter extends RecyclerView.Adapter<AppListAdapter.VH> {
    public interface OnAppClick { void onClick(AppInfo app); }
    private final Context context;
    private final OnAppClick listener;
    private List<AppInfo> apps = new ArrayList<>();
    public AppListAdapter(Context context, OnAppClick listener) {
        this.context = context;
        this.listener = listener;
    }
    public void submit(List<AppInfo> newApps) {
        apps = newApps == null ? new ArrayList<>() : newApps;
        notifyDataSetChanged();
    }
    @Override public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new VH(LayoutInflater.from(context).inflate(R.layout.item_app_entry, parent, false));
    }
    @Override public void onBindViewHolder(VH h, int pos) {
        AppInfo app = apps.get(pos);
        h.icon.setImageDrawable(app.getIcon());
        h.name.setText(app.getName());
        h.pkg.setText(app.getPackageName());
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(app);
        });
    }
    @Override public int getItemCount() { return apps == null ? 0 : apps.size(); }
    static class VH extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, pkg;
        VH(View v) {
            super(v);
            icon = v.findViewById(R.id.iv_app_icon);
            name = v.findViewById(R.id.tv_app_name);
            pkg = v.findViewById(R.id.tv_app_package);
        }
    }
}
