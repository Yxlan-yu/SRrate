package com.yxlanyu.refreshrate.adapter;
import android.content.Context;
import android.graphics.Color;
import android.view.*;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.card.MaterialCardView;
import com.yxlanyu.refreshrate.R;
import com.yxlanyu.refreshrate.model.DisplayMode;
import java.util.List;
public class RateAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int TYPE_HEADER = 0, TYPE_RATE = 1;
    public interface OnRateClick { void onClick(DisplayMode m); }
    private List<Object> items;
    private Context ctx; private OnRateClick listener;
    private DisplayMode current;
    private int lastCurrentPos = -1;
    public RateAdapter(Context c, List<Object> i, OnRateClick l) { ctx=c; items=i; listener=l;
    }
    public void update(List<Object> i) { items=i;
    notifyDataSetChanged(); }
    private static final Object PAYLOAD_SEL = new Object();
    public void setCurrent(DisplayMode m) {
        int oldPos = lastCurrentPos;
        int newPos = findPosition(m);
        current = m;
        lastCurrentPos = newPos;
        if (oldPos != newPos) {
            if (oldPos >= 0) notifyItemChanged(oldPos, PAYLOAD_SEL);
            if (newPos >= 0) notifyItemChanged(newPos, PAYLOAD_SEL);
        }
    }
    public void setCurrentByWHR(int w, int h, int hz) {
        for (Object o : items) {
            if (o instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) o;
                if (dm.getWidth() == w && dm.getHeight() == h && dm.getRateInt() == hz) { setCurrent(dm); return; }
            }
        }
        for (Object o : items) {
            if (o instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) o;
                if (dm.getRateInt() == hz) { setCurrent(dm); return; }
            }
        }
    }
    private int findPosition(DisplayMode target) {
        for (int i = 0; i < items.size(); i++) {
            Object obj = items.get(i);
            if (obj instanceof DisplayMode) {
                DisplayMode dm = (DisplayMode) obj;
                if (dm.getModeId() == target.getModeId() && dm.getRateInt() == target.getRateInt()) {
                    return i;
                }
            }
        }
        return -1;
    }
    @Override public int getItemViewType(int p) { return items.get(p) instanceof String ? TYPE_HEADER : TYPE_RATE; }
    @Override public int getItemCount() { return items==null ? 0 : items.size(); }
    @Override public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup p, int t) {
        LayoutInflater inf = LayoutInflater.from(ctx);
        if (t == TYPE_HEADER) return new HVH(inf.inflate(R.layout.item_res_header, p, false));
        return new RVH(inf.inflate(R.layout.item_rate_card, p, false));
    }
    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos, java.util.List<Object> payloads) {
        if (!payloads.isEmpty() && getItemViewType(pos) == TYPE_RATE) {
            DisplayMode m = (DisplayMode) items.get(pos);
            RVH r = (RVH) h;
            boolean act = current != null && current.getModeId() == m.getModeId() && current.getRateInt() == m.getRateInt();
            applySelection(r, colorFor(m.getRateInt()), act, true);
            return;
        }
        super.onBindViewHolder(h, pos, payloads);
    }
    private void applySelection(RVH r, int color, boolean act, boolean animate) {
        if (act) {
            r.card.setCardBackgroundColor(darkenColor(color, 0.75f));
            r.card.setStrokeColor(0xCCFFFFFF);
            r.card.setStrokeWidth(dpToPx(2));
        } else {
            r.card.setCardBackgroundColor(color);
            r.card.setStrokeColor(0x50FFFFFF);
            r.card.setStrokeWidth(dpToPx(1));
        }
        if (animate && act) {
            r.card.animate().cancel();
            r.card.setScaleX(0.94f); r.card.setScaleY(0.94f);
            r.card.animate().scaleX(1f).scaleY(1f).setDuration(220).setInterpolator(new android.view.animation.OvershootInterpolator(2.5f)).start();
        }
    }
    @Override public void onBindViewHolder(RecyclerView.ViewHolder h, int pos) {
        h.itemView.startAnimation(AnimationUtils.loadAnimation(ctx, R.anim.item_animation));
        if (getItemViewType(pos) == TYPE_HEADER) {
            ((HVH) h).title.setText((String) items.get(pos));
        } else {
            DisplayMode m = (DisplayMode) items.get(pos);
            RVH r = (RVH) h;
            r.name.setText(m.getRateName(ctx));
            r.hz.setText(m.getRateInt() + " Hz");
            r.desc.setText(m.getRateDesc(ctx));
            int color = colorFor(m.getRateInt());
            boolean act = current != null && current.getModeId() == m.getModeId() && current.getRateInt() == m.getRateInt();
            applySelection(r, color, act, false);
            r.card.setOnClickListener(v -> { if (listener != null) listener.onClick(m); });
            r.debug.setVisibility(View.GONE);
        }
    }
    private int colorFor(int hz) {
        if (hz >= 185) return Color.argb(170, 240, 213, 45);
        if (hz >= 165) return Color.argb(170, 230, 100, 150);
        if (hz >= 144) return Color.argb(170, 242, 140, 56);
        if (hz >= 120) return Color.argb(170, 50, 124, 216);
        if (hz >= 90)  return Color.argb(170, 40, 169, 160);
        if (hz >= 60)  return Color.argb(170, 59, 156, 89);
        return Color.argb(170, 67, 160, 71);
    }
    private int darkenColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a, Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }
    private int dpToPx(int dp) {
        return (int) (dp * ctx.getResources().getDisplayMetrics().density);
    }
    static class HVH extends RecyclerView.ViewHolder {
        TextView title;
        HVH(View v) { super(v); title = v.findViewById(R.id.tv_header_title); }
    }
    static class RVH extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView name, hz, desc, debug;
        RVH(View v) {
            super(v);
            card = v.findViewById(R.id.card_rate);
            name = v.findViewById(R.id.tv_rate_name);
            hz = v.findViewById(R.id.tv_rate_hz);
            desc = v.findViewById(R.id.tv_rate_desc);
            debug = v.findViewById(R.id.tv_rate_debug);
        }
    }
}
