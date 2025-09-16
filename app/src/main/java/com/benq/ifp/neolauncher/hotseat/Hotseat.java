package com.benq.ifp.neolauncher.hotseat;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import com.benq.ifp.neolauncher.CellLayout;
import com.benq.ifp.neolauncher.R;
import com.benq.ifp.neolauncher.activity.HomeActivity;
import com.benq.ifp.neolauncher.content.AppMenu;
import com.benq.ifp.neolauncher.widget.AppPieView;

/**
 * 簡易 Hotseat：底部一列固定格數，用於接收 AppPieView 的拖曳丟放。
 * 不放 All Apps 按鈕；圖示從 App List 長按拖過來即可加入。
 */
public class Hotseat extends FrameLayout implements AppPieView.HotseatDropTarget {

    /** Activity（可選用來呼叫功能） */
    private HomeActivity mLauncher;

    /** 內層格子容器（在 hotseat.xml 裡的 @id/layout） */
    private CellLayout mContent;

    /** 橫向（底部一列），不做側邊直欄 */
    private final boolean mHasVerticalHotseat = false;

    /** 預設欄列設定（可依需求修改或提供 setter） */
    private int mColumns = 5;
    private int mRows    = 1;

    public Hotseat(Context context) { this(context, null); }

    public Hotseat(Context context, AttributeSet attrs) { this(context, attrs, 0); }

    public Hotseat(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        if (context instanceof HomeActivity) {
            mLauncher = (HomeActivity) context;
        }
        // 讓自己待在底部（若父層是 FrameLayout 時有用）
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, Gravity.BOTTOM));
        setClickable(true);
        setFocusable(true);
        // 可依需求設背景
        setBackgroundResource(R.color.bg_ui);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mContent = findViewById(R.id.cell_layout);

        if (mContent == null) {
            // 沒有在 XML 放 CellLayout，就自己建一個，避免直接崩潰
            mContent = new CellLayout(getContext());
            mContent.setId(R.id.cell_layout);
            LayoutParams lp = new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            addView(mContent, lp);
        }

        int columns = 5;
        int rows = 1;
        mContent.setGridSize(columns, rows);
        mContent.setIsHotseat(true);

        resetLayout();
    }

    /** 清空 Hotseat 內容 */
    public void resetLayout() {
        if (mContent != null) {
            mContent.removeAllViewsInLayout();
        }
    }

    /** 讓外部（例如 AppPieView 初始化時）能取得 CellLayout */
    public CellLayout getLayout() {
        return mContent;
    }

    /** Hotseat 目前是否有任何圖示 */
    public boolean hasIcons() {
        return mContent != null && mContent.getChildCount() > 0;
    }

    // === AppPieView.HotseatDropTarget ===

    /**
     * 接收從 AppPieView 拖過來的 App。
     * 這裡直接交給 CellLayout 的 addApp(...) 去找空位擺放。
     */
    @Override
    public boolean acceptDrop(AppMenu.AppIcon app) {
        if (mContent == null || app == null) return false;
        return mContent.addApp(app); // 你在 CellLayout 裡完成的實作
    }

    /** 手指是否 hover 在 Hotseat 上，可用來做高亮提示 */
    @Override
    public void onHoverHotseat(boolean hovered) {
        setActivated(hovered);
        // 你也可以在這裡切換不同的背景，或開啟/關閉陰影
        // setBackgroundResource(hovered ? R.drawable.hotseat_bg_highlight : R.color.bg_ui);
    }

    // === 下面是一些可能會用到的工具方法（可選） ===

    /** 把順序轉換成 Cell X（水平 Hotseat 用 rank→x） */
    public int getCellXFromOrder(int rank) {
        return mHasVerticalHotseat ? 0 : rank;
    }

    /** 把順序轉換成 Cell Y（水平 Hotseat 永遠 0 列） */
    public int getCellYFromOrder(int rank) {
        if (mContent == null) return 0;
        return mHasVerticalHotseat ? (mContent.getCountY() - (rank + 1)) : 0;
    }

    /** 允許外部調整欄列數（可在 onFinishInflate 前後設定） */
    public void setGridSize(int columns, int rows) {
        mColumns = Math.max(1, columns);
        mRows    = Math.max(1, rows);
        if (mContent != null) {
            mContent.setGridSize(mColumns, mRows);
        }
    }

    /** 允許外部直接加入 App（非拖曳路徑） */
    public boolean addApp(AppMenu.AppIcon app) {
        return mContent != null && mContent.addApp(app);
    }

    /** 提供簡單點擊反饋（抓到 Hotseat 根 view 的點擊） */
    @Override
    public boolean performClick() {
        super.performClick();
        // 預設不做事，留給子 view（icon）處理
        return true;
    }

    /** 讓父層可以 block 事件（例如 Workspace 在 modal 狀態時） */
    @Override
    public boolean onInterceptTouchEvent(android.view.MotionEvent ev) {
        // 需要攔截時可在這裡加上條件判斷
        return super.onInterceptTouchEvent(ev);
    }
}
