package com.benq.ifp.neolauncher.hotseat;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.benq.ifp.neolauncher.CellLayout;
import com.benq.ifp.neolauncher.R;
import com.benq.ifp.neolauncher.activity.HomeActivity;
import com.benq.ifp.neolauncher.content.AppMenu;
import com.benq.ifp.neolauncher.widget.AppPieView;

/**
 * 簡易 Hotseat：底部一列固定格數，用於接收 AppPieView 的拖曳丟放。
 */
public class Hotseat extends FrameLayout implements AppPieView.HotseatDropTarget {

    private HomeActivity mLauncher;
    private CellLayout mContent;

    private final boolean mHasVerticalHotseat = false;
    private int mColumns = 5;
    private int mRows    = 1;

    public Hotseat(Context context) { this(context, null); }
    public Hotseat(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (context instanceof HomeActivity) {
            mLauncher = (HomeActivity) context;
        }
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        setClickable(true);
        setFocusable(true);

        // 底色：淡色背景
        setBackgroundResource(R.color.bg_ui);

        // 高亮層：新機用 foreground、舊機用 background selector 退回
        if (Build.VERSION.SDK_INT >= 23) {
            setForeground(ContextCompat.getDrawable(getContext(), R.drawable.hotseat_foreground));
        } else {
            // API < 23 沒有通用 View#setForeground，退回用 background selector 呈現高亮
            setBackgroundResource(R.drawable.hotseat_foreground);
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContent = findViewById(R.id.cell_layout);
        if (mContent == null) {
            mContent = new CellLayout(getContext());
            mContent.setId(R.id.cell_layout);
            LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(mContent, lp);
        }
        mContent.setGridSize(5, 1);
        mContent.setIsHotseat(true);
        resetLayout();
    }

    /** 清空 Hotseat 內容 */
    public void resetLayout() {
        if (mContent != null) mContent.removeAllViewsInLayout();
    }

    public CellLayout getLayout() { return mContent; }

    public boolean hasIcons() { return mContent != null && mContent.getChildCount() > 0; }

    // ==== DropTarget ====

    @Override
    public boolean acceptDrop(AppMenu.AppIcon app) {
        if (mContent == null || app == null) return false;
        return mContent.addApp(app);
    }

    @Override
    public void onHoverHotseat(boolean hovered) {
        setActivated(hovered);           // 觸發 selector 狀態

        // 額外可見回饋（動畫 + 陰影）
        animate()
                .scaleX(hovered ? 1.02f : 1f)
                .scaleY(hovered ? 1.02f : 1f)
                .setDuration(120)
                .start();
        if (Build.VERSION.SDK_INT >= 21) {
            setElevation(hovered ? 12f : 0f);
        }
    }

    // 讓自訂/selector 都能確實收到狀態變化
    @Override
    public void setActivated(boolean activated) {
        boolean changed = activated != isActivated();
        super.setActivated(activated);
        if (changed) {
            refreshDrawableState();
            invalidate();
        }
    }

    // ==== 工具 ====

    public int getCellXFromOrder(int rank) { return mHasVerticalHotseat ? 0 : rank; }

    public int getCellYFromOrder(int rank) {
        if (mContent == null) return 0;
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    public void setGridSize(int columns, int rows) {
        mColumns = Math.max(1, columns);
        mRows    = Math.max(1, rows);
        if (mContent != null) mContent.setGridSize(mColumns, mRows);
    }

    public boolean addApp(AppMenu.AppIcon app) {
        return mContent != null && mContent.addApp(app);
    }

    @Override
    public boolean performClick() {
        super.performClick();
        return true; // 交給子 View 處理
    }

}
