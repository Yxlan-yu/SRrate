package com.yxlanyu.refreshrate;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
public class WallpaperStore {
    private static final String FILE_NAME = "wallpaper_img";
    private static Bitmap cached = null;
    public static java.io.File file(Context ctx) {
        return new java.io.File(ctx.getFilesDir(), FILE_NAME);
    }
    public static boolean saveFromUri(Context ctx, Uri uri) {
        java.io.InputStream is = null;
        java.io.FileOutputStream fos = null;
        java.io.File tmp = new java.io.File(ctx.getFilesDir(), FILE_NAME + ".tmp");
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int len;
            long total = 0;
            while ((len = is.read(buf)) != -1) { fos.write(buf, 0, len); total += len; }
            fos.flush();
            if (total <= 0) { tmp.delete(); return false; }
            java.io.File dst = file(ctx);
            if (dst.exists()) dst.delete();
            if (!tmp.renameTo(dst)) { tmp.delete(); return false; }
            cached = null;
            ctx.getSharedPreferences("s", Context.MODE_PRIVATE).edit()
                    .putString("wallpaper_path", dst.getAbsolutePath()).apply();
            return true;
        } catch (Exception e) {
            try { tmp.delete(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (fos != null) fos.close(); } catch (Exception ignored) {}
        }
    }
    public static Bitmap load(Context ctx) {
        if (cached != null && !cached.isRecycled()) return cached;
        try {
            java.io.File f = file(ctx);
            if (f.exists() && f.length() > 0) {
                Bitmap bm = decodeScaled(f);
                if (bm != null) { cached = bm; return bm; }
            }
            String uriStr = ctx.getSharedPreferences("s", Context.MODE_PRIVATE).getString("wallpaper_uri", null);
            if (uriStr != null) {
                if (saveFromUri(ctx, Uri.parse(uriStr))) {
                    Bitmap bm = decodeScaled(file(ctx));
                    if (bm != null) { cached = bm; return bm; }
                }
            }
        } catch (Exception ignored) {}
        return null;
    }
    private static final java.util.HashMap<String, Bitmap> pageCache = new java.util.HashMap<String, Bitmap>();
    public static java.io.File pageFile(Context ctx, String key) {
        return new java.io.File(ctx.getFilesDir(), "wp_page_" + key);
    }
    public static boolean savePageFromUri(Context ctx, Uri uri, String key) {
        java.io.InputStream is = null;
        java.io.FileOutputStream fos = null;
        java.io.File tmp = new java.io.File(ctx.getFilesDir(), "wp_page_" + key + ".tmp");
        try {
            is = ctx.getContentResolver().openInputStream(uri);
            if (is == null) return false;
            fos = new java.io.FileOutputStream(tmp);
            byte[] buf = new byte[8192];
            int len;
            long total = 0;
            while ((len = is.read(buf)) != -1) { fos.write(buf, 0, len); total += len; }
            fos.flush();
            if (total <= 0) { tmp.delete(); return false; }
            java.io.File dst = pageFile(ctx, key);
            if (dst.exists()) dst.delete();
            if (!tmp.renameTo(dst)) { tmp.delete(); return false; }
            pageCache.remove(key);
            return true;
        } catch (Exception e) {
            try { tmp.delete(); } catch (Exception ignored) {}
            return false;
        } finally {
            try { if (is != null) is.close(); } catch (Exception ignored) {}
            try { if (fos != null) fos.close(); } catch (Exception ignored) {}
        }
    }
    public static Bitmap loadPage(Context ctx, String key) {
        Bitmap c = pageCache.get(key);
        if (c != null && !c.isRecycled()) return c;
        try {
            java.io.File f = pageFile(ctx, key);
            if (f.exists() && f.length() > 0) {
                Bitmap bm = decodeScaled(f);
                if (bm != null) { pageCache.put(key, bm); return bm; }
            }
        } catch (Exception ignored) {}
        return null;
    }
    public static boolean hasPage(Context ctx, String key) {
        java.io.File f = pageFile(ctx, key);
        return f.exists() && f.length() > 0;
    }
    public static void clearPage(Context ctx, String key) {
        try { java.io.File f = pageFile(ctx, key); if (f.exists()) f.delete(); } catch (Exception ignored) {}
        pageCache.remove(key);
    }
    public static android.graphics.drawable.Drawable presetDrawable(Context ctx, int type) {
        switch (type) {
            case 1: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_2);
            case 2: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_3);
            case 6: return androidx.core.content.ContextCompat.getDrawable(ctx, R.drawable.wallpaper_gradient_1);
            case 5: return new android.graphics.drawable.ColorDrawable(0xFF000000);
            case 7: return new android.graphics.drawable.ColorDrawable(0xFF4FC3F7);
            case 8: return new android.graphics.drawable.ColorDrawable(0xFFF48FB1);
            case 9: return new android.graphics.drawable.ColorDrawable(0xFF80CBC4);
            case 10: return new android.graphics.drawable.ColorDrawable(0xFFB39DDB);
            default: return new android.graphics.drawable.ColorDrawable(0xFFFFFFFF);
        }
    }
    private static Bitmap decodeScaled(java.io.File f) {
        try {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(f.getAbsolutePath(), o);
            int maxSide = 2400;
            int sample = 1;
            int w = o.outWidth, h = o.outHeight;
            while (w / sample > maxSide || h / sample > maxSide) sample *= 2;
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = sample;
            return BitmapFactory.decodeFile(f.getAbsolutePath(), o2);
        } catch (Exception e) { return null; }
    }
}
