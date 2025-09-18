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
    }
    public void draw(Canvas canvas) {
        // 畫背景 bar
        paintBg.setColor(Color.argb(160, 0, 0, 0));
        canvas.drawRect(barRect, paintBg);

        int cx = barRect.centerX();
        int cy = barRect.centerY();

        // 四個 slot 的總寬度
        int totalSlots = 4;
        int totalWidth = slotW * totalSlots;

        // 從中心點向左右展開，再整體往右偏移 500px
        int offsetX = 500;
        int startX = cx - totalWidth / 2 + offsetX;
        int y = barRect.top;

        // 分別畫 slot
        drawSlot(canvas, new Rect(startX, y, startX + slotW, y + slotH), bmpCasting);
        drawSlot(canvas, new Rect(startX + slotW, y, startX + 2 * slotW, y + slotH), bmpSettings);
        drawSlot(canvas, new Rect(startX + 2 * slotW, y, startX + 3 * slotW, y + slotH), bmpAllApps);
        drawSlot(canvas, new Rect(startX + 3 * slotW, y, startX + 4 * slotW, y + slotH), bmpHelp);

        // userName 不動，還是畫在左邊
        paintText.setColor(Color.WHITE);
        paintText.setTextSize(24f * ctx.getResources().getDisplayMetrics().density);
        canvas.drawText(userName, barRect.left + 20, cy + (paintText.getTextSize() / 2), paintText);
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


    /** 處理點擊事件 */
    public boolean handleTouch(int x, int y, HomeActivity mLauncher) {
        if (!barRect.contains(x, y)) return false;

        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotRects[i].contains(x, y)) {
                switch (i) {
                    case 0: // Casting
                       Log.d("CanvasMenuBar", "handleTouch: Casting");
                        break;
                    case 1: // Settings
                        Log.d("CanvasMenuBar", "handleTouch: Settings");
                        break;
                    case 2: // AllApps
                        Log.d("CanvasMenuBar", "handleTouch: AllApps");
                        if (mLauncher != null) mLauncher.showAllApps();
                        break;
                    case 3: // Help
                        Log.d("CanvasMenuBar", "handleTouch: AllApps");
                        break;
                }
                return true;
            }
        }
        return false;
    }
}
