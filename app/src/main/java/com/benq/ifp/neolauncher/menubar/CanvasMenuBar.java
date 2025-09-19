package com.benq.ifp.neolauncher.menubar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.DrawableRes;

import com.benq.ifp.neolauncher.R;
import com.benq.ifp.neolauncher.activity.HomeActivity;
import com.benq.ifp.neolauncher.graphics.Converter;

public class CanvasMenuBar {
    private static final int SLOT_COUNT = 4; // Casting, Setting, AllApps, Help

    private final Rect barRect = new Rect();
    private final Rect[] slotRects = new Rect[SLOT_COUNT];
    private final Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Bitmap bmpCasting, bmpSettings, bmpAllApps, bmpHelp;
    private String userName = "admin";
    private Context ctx;
    private int slotH, slotW;

    public CanvasMenuBar(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        p.setFilterBitmap(true);
        paintText.setColor(0xffffffff);
        paintText.setTextSize(36f);
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotRects[i] = new Rect();
        }
        loadIconsIfNeeded();
    }

    private void loadIconsIfNeeded() {
        if (bmpCasting == null || bmpCasting.isRecycled()) {
            bmpCasting = safeDecode(R.drawable.source_52);
        }
        if (bmpSettings == null || bmpSettings.isRecycled()) {
            bmpSettings = safeDecode(R.drawable.setting_52);
        }
        if (bmpAllApps == null || bmpAllApps.isRecycled()) {
            bmpAllApps = safeDecode(R.drawable.all_apps_52);
        }
        if (bmpHelp == null || bmpHelp.isRecycled()) {
            bmpHelp = safeDecode(R.drawable.help_52);
        }
    }

    private Bitmap safeDecode(@DrawableRes int resId) {
        try {
            return Converter.getBitmapFromDrawable(ctx.getResources(), resId);
        } catch (Throwable t) {
            return null;
        }
    }

    public void layout(int screenW, int screenH) {
        // bar 高度 = XML 設定的 menu_bar_icon_container_height (大概 50dp)
        int barH = ctx.getResources().getDimensionPixelSize(R.dimen.menu_bar_container_height);

        int bottom = screenH;
        int top    = bottom - barH;
        barRect.set(0, top, screenW, bottom);

        // slot 高度和 bar 一致
        slotH = barH;
        slotW = barH; // 如果要正方形 slot
        Log.d("CanvasMenuBar", "layout() barRect = " + barRect.toShortString());
    }

    public boolean hitTest(int x, int y) {
        return getAllSlotsRect().contains(x, y);
    }


    public void draw(Canvas canvas) {
        paintBg.setColor(Color.argb(160, 0, 0, 0));
        canvas.drawRect(barRect, paintBg);

        int cx = barRect.centerX();
        int cy = barRect.centerY();

        int totalWidth = slotW * SLOT_COUNT;
        int offsetX = 500;
        int startX = cx - totalWidth / 2 + offsetX;
        int y = barRect.top;

        // 設定 slotRects 並畫 icon
        for (int i = 0; i < SLOT_COUNT; i++) {
            int left = startX + i * slotW;
            int right = left + slotW;
            Rect rect = slotRects[i];
            rect.set(left, y, right, y + slotH); // ★★ 這一行很重要！

            Bitmap bmp = null;
            switch (i) {
                case 0: bmp = bmpCasting; break;
                case 1: bmp = bmpSettings; break;
                case 2: bmp = bmpAllApps; break;
                case 3: bmp = bmpHelp; break;
            }

            drawSlot(canvas, rect, bmp);
        }

        // 畫 userName 文字
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(24f * ctx.getResources().getDisplayMetrics().density);
        canvas.drawText(userName, barRect.left + 20, cy + (paintText.getTextSize() / 2), paintText);
    }

    /**
     * 回傳一個 Rect，包含所有 slot 的範圍。
     * 注意：必須在 draw() 執行過至少一次之後才會準確，
     * 因為 slotRects[] 是在 draw() 裡 set 的。
     */
    public Rect getAllSlotsRect() {
        if (slotRects[0].isEmpty() || slotRects[SLOT_COUNT - 1].isEmpty()) {
            // 還沒初始化，回傳 barRect 當 fallback
            return new Rect(barRect);
        }
        return new Rect(
                slotRects[0].left,
                barRect.top,
                slotRects[SLOT_COUNT - 1].right,
                barRect.bottom
        );
    }




    private void drawSlot(Canvas canvas, Rect dst, Bitmap bmp) {
        if (bmp == null || bmp.isRecycled()) {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            canvas.drawRoundRect(new RectF(dst), 12f, 12f, p);
            return;
        }

        // ==== 固定 ICON SIZE (例如 48dp) ====
        int iconSize = (int) (48 * ctx.getResources().getDisplayMetrics().density);

        int cx = dst.centerX();
        int cy = dst.centerY();
        int half = iconSize / 2;

        Rect iconRect = new Rect(
                cx - half,
                cy - half,
                cx + half,
                cy + half
        );

        canvas.drawBitmap(bmp, null, iconRect, p);
    }


    public boolean handleTouch(int x, int y, HomeActivity launcher) {
        Log.d("CanvasMenuBar", "handleTouch() touch=(" + x + "," + y + ")");
        if (!barRect.contains(x, y)) {
            Log.d("CanvasMenuBar", "touch not in barRect");
            return false;
        }

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotRects[i].contains(x, y)) {
                Log.d("CanvasMenuBar", "hit slot[" + i + "]");
                switch (i) {
                    case 0: Log.d("CanvasMenuBar", "Casting clicked"); break;
                    case 1: Log.d("CanvasMenuBar", "Settings clicked"); break;
                    case 2:
                        Log.d("CanvasMenuBar", "AllApps clicked");
                        if (launcher != null) launcher.showAllApps();
                        break;
                    case 3: Log.d("CanvasMenuBar", "Help clicked"); break;
                }
                return true;
            }
        }

        Log.d("CanvasMenuBar", "no slot hit");
        return false;
    }

}
