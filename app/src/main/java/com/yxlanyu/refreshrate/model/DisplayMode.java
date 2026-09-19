package com.yxlanyu.refreshrate.model;
import android.content.Context;
import com.yxlanyu.refreshrate.R;
public class DisplayMode {
    private int width, height, modeId;
    private float refreshRate;
    private int sfIndex = -1;
    public DisplayMode(int w, int h, float r, int id) {
        width=w;
        height=h; refreshRate=r; modeId=id;
    }
    public int getWidth(){return width;}
    public int getHeight(){return height;}
    public float getRefreshRate(){return refreshRate;}
    public int getModeId(){return modeId;}
    public int getSfIndex(){return sfIndex;}
    public void setSfIndex(int i){sfIndex=i;}
    public String getResolutionLabel(){return width+"x"+height;}
    public int getRateInt(){return Math.round(refreshRate);}
    public String getRateName(Context ctx) {
    int r = getRateInt();
    if (r >= 185) return ctx.getString(R.string.rate_name_supreme);
    if (r >= 165) return ctx.getString(R.string.rate_name_limit);
    if (r >= 144) return ctx.getString(R.string.rate_name_high);
    if (r >= 120) return ctx.getString(R.string.rate_name_extreme);
    if (r >= 90)  return ctx.getString(R.string.rate_name_excellent);
    if (r >= 60)  return ctx.getString(R.string.rate_name_smooth);
    return ctx.getString(R.string.rate_name_standard);
}
public String getRateDesc(Context ctx) {
    int r = getRateInt();
    if (r >= 185) return ctx.getString(R.string.rate_desc_supreme);
    if (r >= 165) return ctx.getString(R.string.rate_desc_limit);
    if (r >= 144) return ctx.getString(R.string.rate_desc_high);
    if (r >= 120) return ctx.getString(R.string.rate_desc_extreme);
    if (r >= 90)  return ctx.getString(R.string.rate_desc_excellent);
    if (r >= 60)  return ctx.getString(R.string.rate_desc_smooth);
    return ctx.getString(R.string.rate_desc_standard);
}
}
