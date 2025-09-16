package com.benq.ifp.neolauncher;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.benq.ifp.neolauncher.app.PieLauncherApp;
import com.benq.ifp.neolauncher.content.AppMenu;

/**
 * 極簡 CellLayout：用 GridLayout 當底層，支援 Hotseat 需要的 API。
 */
public class CellLayout extends GridLayout {
    private int mCountX = 5;
    private int mCountY = 1;
    private boolean mIsHotseat = false;

    private int removingDepth = 0; // 用深度而不是 boolean，比較不會出現卡死
    private static final String TAG = "CellLayout";

    public CellLayout(Context context) { this(context, null); }
    public CellLayout(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public CellLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setUseDefaultMargins(false);
        setAlignmentMode(GridLayout.ALIGN_BOUNDS);
        setColumnCount(mCountX);
        setRowCount(mCountY);
    }

    public boolean addApp(AppMenu.AppIcon app) {
        if (app == null) return false;

        View item = LayoutInflater.from(getContext())
                .inflate(R.layout.hotseat_icon, this, false);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageView iv = item.findViewById(R.id.icon);
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) TextView  tv = item.findViewById(R.id.label);

        // 1) 圖示：先用索引時就準備好的 bitmap
        if (app.bitmap != null) {
            iv.setImageBitmap(app.bitmap);
        } else {
            // 2) 萬一沒有，改用 PM 依 componentName 取圖示
            Drawable d = null;
            try {
                d = getContext().getPackageManager().getActivityIcon(app.componentName);
            } catch (PackageManager.NameNotFoundException ignore) {}
            if (d != null) {
                iv.setImageDrawable(d);
            } else {
                // 3) 仍失敗就用預設圖
                iv.setImageResource(R.mipmap.ic_launcher);
            }
        }

        // 標籤
        CharSequence label = !TextUtils.isEmpty(app.label)
                ? app.label
                : app.componentName.getShortClassName();
        tv.setText(label);
        item.setContentDescription(label);

        // 點擊啟動
        item.setOnClickListener(v -> PieLauncherApp.appMenu.launchApp(getContext(), app));

        // 找第一個空格位
        int[] cell = findFirstVacantCell(); // 你自己的輔助方法（上一版我給過）
        if (cell == null) return false;

        CellLayout.LayoutParams lp = new CellLayout.LayoutParams(cell[0], cell[1], 1, 1);
        lp.canReorder = false;
        item.setId(View.generateViewId());

        // 如果這段在 CellLayout 內：
        try {
            addViewToCellLayout(item, -1, item.getId(), lp, true);
        } catch (Throwable t) {
            addView(item, lp);
        }

        // 如果這段在 Hotseat 內，請用：
        // mContent.addViewToCellLayout(item, -1, item.getId(), lp, true);
        return false;
    }

    private Drawable resolveIconFromPm(Context ctx, AppMenu.AppIcon app) {
        if (app == null) return null;

        // 1) 優先用索引階段已轉好的 bitmap（最穩、最快）
        if (app.bitmap != null) {
            return new BitmapDrawable(ctx.getResources(), app.bitmap);
        }

        try {
            // 2) Drawer 是假元件，不可用 PM 取圖，用內建/圖包
            if (PieLauncherApp.appMenu.isDrawerIcon(app)) {
                Drawable d = PieLauncherApp.iconPack.getIcon(
                        new ComponentName(ctx.getPackageName() + ".drawer", "Drawer"));
                if (d == null) {
                    d = ctx.getResources().getDrawable(R.drawable.ic_drawer);
                }
                return d;
            }

            // 3) 一般 App：先試 Activity 圖示，再退到 Application 圖示
            PackageManager pm = ctx.getPackageManager();
            if (app.componentName != null) {
                Drawable d = pm.getActivityIcon(app.componentName);
                if (d != null) return d;

                String pkg = app.componentName.getPackageName();
                if (!TextUtils.isEmpty(pkg)) {
                    d = pm.getApplicationIcon(pkg);
                    if (d != null) return d;
                }
            }
        } catch (Exception ignore) { }
        return null;
    }

    private CharSequence resolveLabelFromPm(Context ctx, AppMenu.AppIcon app) {
        if (app == null) return "";
        // 1) 先用索引好的 label（AppMenu 建立時就寫好了）
        if (!TextUtils.isEmpty(app.label)) return app.label;

        try {
            if (PieLauncherApp.appMenu.isDrawerIcon(app)) {
                // Drawer 顯示自訂字串
                return "All apps"; // 或 "All apps"
            }

            PackageManager pm = ctx.getPackageManager();
            if (app.componentName != null) {
                // 2) 先取 Activity label
                ActivityInfo ai = pm.getActivityInfo(app.componentName, 0);
                if (ai != null) {
                    CharSequence l = ai.loadLabel(pm);
                    if (!TextUtils.isEmpty(l)) return l;
                }
                // 3) 退回 Application label
                String pkg = app.componentName.getPackageName();
                if (!TextUtils.isEmpty(pkg)) {
                    ApplicationInfo appInfo = pm.getApplicationInfo(pkg, 0);
                    CharSequence l2 = appInfo.loadLabel(pm);
                    if (!TextUtils.isEmpty(l2)) return l2;
                }
            }
        } catch (Exception ignore) { }

        // 最後保底：用 packageName 當顯示文字
        return app.componentName != null ? app.componentName.getPackageName() : "";
    }
    private int[] findFirstVacantCell() {
        int cx = getCountX();
        int cy = getCountY();
        boolean[][] occ = new boolean[cx][cy];

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (!(child.getLayoutParams() instanceof CellLayout.LayoutParams)) continue;
            CellLayout.LayoutParams lp = (CellLayout.LayoutParams) child.getLayoutParams();
            int x0 = Math.max(0, lp.cellX);
            int y0 = Math.max(0, lp.cellY);
            int x1 = Math.min(cx, lp.cellX + Math.max(1, lp.cellHSpan));
            int y1 = Math.min(cy, lp.cellY + Math.max(1, lp.cellVSpan));
            for (int x = x0; x < x1; x++) {
                for (int y = y0; y < y1; y++) {
                    occ[x][y] = true;
                }
            }
        }

        for (int y = 0; y < cy; y++) {
            for (int x = 0; x < cx; x++) {
                if (!occ[x][y]) return new int[]{x, y};
            }
        }
        return null;
    }


    public void setGridSize(int countX, int countY) {
        mCountX = Math.max(1, countX);
        mCountY = Math.max(1, countY);
        setColumnCount(mCountX);
        setRowCount(mCountY);
        requestLayout();
    }

    public int getCountX() { return mCountX; }
    public int getCountY() { return mCountY; }

    public void setIsHotseat(boolean isHotseat) {
        mIsHotseat = isHotseat;
        // 需要特別樣式可在這裡調整（背景、padding…）
    }

    /** AOSP 是回傳 ShortcutAndWidgetContainer；這裡直接回傳自己就好。 */
    public ViewGroup getShortcutsAndWidgets() {
        return this;
    }

    /** 相容 AOSP 的 LayoutParams。 */
    public static class LayoutParams extends GridLayout.LayoutParams {
        public int cellX, cellY;
        public int cellHSpan = 1, cellVSpan = 1;
        public boolean canReorder = true;

        public LayoutParams(int cellX, int cellY, int hSpan, int vSpan) {
            super(spec(cellY, vSpan), spec(cellX, hSpan));
            this.cellX = cellX;
            this.cellY = cellY;
            this.cellHSpan = hSpan;
            this.cellVSpan = vSpan;
            width = 0;   // 讓 GridLayout 依欄寬平分
            height = 0;  // 讓 GridLayout 依列高平分
        }
    }

    /**
     * 相容 AOSP 的新增方法。
     * @param child 要加的 View
     * @param index 通常 -1
     * @param childId 你要指定給 child 的 id（或 0 忽略）
     * @param lp 位置/跨度
     * @param markCells 這裡不做占格標記，直接忽略
     */
    public boolean addViewToCellLayout(View child, int index, int childId,
                                       LayoutParams lp, boolean markCells) {
        if (childId != 0) {
            child.setId(childId);
        }
        // 邊界檢查（避免超出網格）
        if (lp.cellX < 0 || lp.cellY < 0
                || lp.cellX + lp.cellHSpan > mCountX
                || lp.cellY + lp.cellVSpan > mCountY) {
            return false;
        }
        // 轉成 GridLayout 的 spec
        GridLayout.Spec row = GridLayout.spec(lp.cellY, lp.cellVSpan, GridLayout.FILL);
        GridLayout.Spec col = GridLayout.spec(lp.cellX, lp.cellHSpan, GridLayout.FILL);
        GridLayout.LayoutParams glp = new GridLayout.LayoutParams(row, col);
        glp.width = 0;
        glp.height = 0;
        glp.setMargins(0, 0, 0, 0);
        child.setLayoutParams(glp);

        if (index >= 0 && index < getChildCount()) {
            addView(child, index);
        } else {
            addView(child);
        }
        return true;
    }


    @Override
    public void removeAllViews() {
        if (removingDepth > 0) {
            // 已在移除流程中，再次呼叫一律忽略，避免遞迴
            android.util.Log.w(TAG, "removeAllViews() re-enter blocked. depth=" + removingDepth,
                    new Exception("reenter"));
            return;
        }
        removingDepth++;
        try {
            super.removeAllViews(); // 內部會自行呼叫 removeAllViewsInLayout() + requestLayout()
        } finally {
            removingDepth--;
        }
    }
}
