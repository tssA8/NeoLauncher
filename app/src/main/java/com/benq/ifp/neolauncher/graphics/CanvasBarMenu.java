package com.benq.ifp.neolauncher.graphics;

import android.graphics.Rect;

import com.benq.ifp.neolauncher.content.AppMenu;

public class CanvasBarMenu extends CanvasPieMenu {
    private static final int HOTSEAT_COLS = 5; // 5格
    private Rect barRect = new Rect(); // 整個長條區域

    public void setBarRect(Rect rect) {
        this.barRect.set(rect);
    }

    // 新的方法，不是 override
    public void layoutIconsAsBar() {
        if (icons == null || icons.isEmpty()) return;

        int slotW = barRect.width() / HOTSEAT_COLS;
        int slotH = barRect.height();

        for (int i = 0; i < icons.size() && i < HOTSEAT_COLS; i++) {
            CanvasIcon icon = (CanvasIcon) icons.get(i);
            icon.x = barRect.left + slotW * i + slotW / 2;
            icon.y = barRect.top + slotH / 2;
            icon.size = Math.min(slotW, slotH) * 0.6;
        }
    }
}
