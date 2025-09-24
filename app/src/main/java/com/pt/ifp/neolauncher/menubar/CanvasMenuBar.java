package com.pt.ifp.neolauncher.menubar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import androidx.annotation.DrawableRes;

import com.pt.ifp.neolauncher.R;
import com.pt.ifp.neolauncher.activity.HomeActivity;
import com.pt.ifp.neolauncher.graphics.Converter;

public class CanvasMenuBar {
    private static final int SLOT_COUNT = 4; // Casting, Setting, AllApps, Help
    private static final int ICON_SIZE_SLOT_DP = 30;  // 四顆功能 icon
    private static final int ICON_SIZE_USER_DP = 24;  // user 人頭 icon
    private static final int TEXT_SIZE_USER_SP = 12;  // user 文字大小

    private final Rect barRect = new Rect();
    private final Rect[] slotRects = new Rect[SLOT_COUNT];
    private final Paint paintBg = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint paintText = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Bitmap bmpCasting, bmpSettings, bmpAllApps, bmpHelp, bmpUser;
    private String userName = "admin";
    private final Context ctx;
    private int slotH, slotW;

    public CanvasMenuBar(Context ctx) {
        this.ctx = ctx.getApplicationContext();
        p.setFilterBitmap(true);
        paintText.setColor(Color.WHITE);
        for (int i = 0; i < SLOT_COUNT; i++) {
            slotRects[i] = new Rect();
        }
        loadIconsIfNeeded();
    }

    private void loadIconsIfNeeded() {
        if (bmpCasting == null || bmpCasting.isRecycled())
            bmpCasting = safeDecode(R.drawable.source_52);
        if (bmpSettings == null || bmpSettings.isRecycled())
            bmpSettings = safeDecode(R.drawable.setting_52);
        if (bmpAllApps == null || bmpAllApps.isRecycled())
            bmpAllApps = safeDecode(R.drawable.all_apps_52);
        if (bmpHelp == null || bmpHelp.isRecycled())
            bmpHelp = safeDecode(R.drawable.help_52);
        if (bmpUser == null || bmpUser.isRecycled())
            bmpUser = safeDecode(R.drawable.svg_icon_login_user);
    }

    private Bitmap safeDecode(@DrawableRes int resId) {
        try {
            return Converter.getBitmapFromDrawable(ctx.getResources(), resId);
        } catch (Throwable t) {
            return null;
        }
    }

    public void layout(int screenW, int screenH) {
        int barH = ctx.getResources().getDimensionPixelSize(R.dimen.menu_bar_container_height);
        int bottom = screenH;
        int top = bottom - barH;
        barRect.set(0, top, screenW, bottom);

        slotH = barH;
        slotW = barH; // 每格寬度（正方形 slot）
        Log.d("CanvasMenuBar", "layout() barRect = " + barRect.toShortString());
    }

    public boolean hitTest(int x, int y) {
        return getAllSlotsRect().contains(x, y);
    }

    public void draw(Canvas canvas) {
        paintBg.setColor(Color.argb(160, 0, 0, 0));
        canvas.drawRect(barRect, paintBg);

        int cx = barRect.centerX();
        int totalWidth = slotW * SLOT_COUNT;
        int offsetX = 500; // 若要微調水平位置
        int startX = cx - totalWidth / 2 + offsetX;
        int y = barRect.top;

        // 畫四顆功能 icon
        for (int i = 0; i < SLOT_COUNT; i++) {
            int left = startX + i * slotW;
            int right = left + slotW;
            Rect rect = slotRects[i];
            rect.set(left, y, right, y + slotH);

            Bitmap bmp = null;
            switch (i) {
                case 0: bmp = bmpCasting; break;
                case 1: bmp = bmpSettings; break;
                case 2: bmp = bmpAllApps; break;
                case 3: bmp = bmpHelp; break;
            }
            drawSlot(canvas, rect, bmp);
        }

        // 畫左下角 user info
        drawUserInfo(canvas);
    }

    /** 包含所有 slot 的範圍 */
    public Rect getAllSlotsRect() {
        if (slotRects[0].isEmpty() || slotRects[SLOT_COUNT - 1].isEmpty()) {
            return new Rect(barRect);
        }
        return new Rect(slotRects[0].left, barRect.top,
                slotRects[SLOT_COUNT - 1].right, barRect.bottom);
    }

    /** 畫單一功能 icon slot */
    private void drawSlot(Canvas canvas, Rect dst, Bitmap bmp) {
        int iconSize = (int) (ICON_SIZE_SLOT_DP * ctx.getResources().getDisplayMetrics().density);
        int cx = dst.centerX();
        int cy = dst.centerY();
        int half = iconSize / 2;

        Rect iconRect = new Rect(cx - half, cy - half, cx + half, cy + half);

        if (bmp != null && !bmp.isRecycled()) {
            canvas.drawBitmap(bmp, null, iconRect, p);
        } else {
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(2f);
            canvas.drawRoundRect(new RectF(dst), 12f, 12f, p);
        }
    }

    /** 畫 user icon + 名稱 */
    private void drawUserInfo(Canvas canvas) {
        float density = ctx.getResources().getDisplayMetrics().density;
        float scaledDensity = ctx.getResources().getDisplayMetrics().scaledDensity;

        int marginStart = (int) (16 * density);
        int iconSize = (int) (ICON_SIZE_USER_DP * density);
        int textSize = (int) (TEXT_SIZE_USER_SP * scaledDensity);

        paintText.setColor(Color.WHITE);
        paintText.setTextSize(textSize);

        // 人頭位置：左側 marginStart，垂直置中
        int centerY = barRect.centerY();
        int left = barRect.left + marginStart;
        Rect userRect = new Rect(left, centerY - iconSize / 2,
                left + iconSize, centerY + iconSize / 2);

        if (bmpUser != null && !bmpUser.isRecycled()) {
            canvas.drawBitmap(bmpUser, null, userRect, p);
        } else {
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.WHITE);
            canvas.drawCircle(userRect.centerX(), userRect.centerY(), iconSize / 2f, p);
        }

        // 文字：在人頭右邊 8dp，垂直置中
        float textX = userRect.right + (8 * density);
        float textY = userRect.centerY() - ((paintText.descent() + paintText.ascent()) / 2);
        canvas.drawText(userName, textX, textY, paintText);
    }

    public boolean handleTouch(int x, int y, HomeActivity launcher) {
        Log.d("CanvasMenuBar", "handleTouch() touch=(" + x + "," + y + ")");
        if (!barRect.contains(x, y)) {
            return false;
        }
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (slotRects[i].contains(x, y)) {
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
        return false;
    }
}
