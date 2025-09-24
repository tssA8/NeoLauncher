package com.pt.ifp.neolauncher.recommend;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;

import androidx.core.content.ContextCompat;

import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.pt.ifp.neolauncher.R;


public class RecommendRowItemView extends FrameLayout {

    private final int mImageId;
    private final int mImageIdFocus;
    private final float mScale;
    private final float mTextUnfocusAlpha;
    private final int mAnimationTime;
    private final ImageView ivImage;
    private final TextView tvTitle;
    private final FrameLayout mRootView;
    private Context mContext;

    public RecommendRowItemView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setFocusable(true);
        setClickable(true);
        mContext = context;
        mScale = context.getResources().getDimension(R.dimen.recommend_row_item_animation_scale);
        mTextUnfocusAlpha = context.getResources().getDimension(R.dimen.recommend_row_item_title_alpha_unfocus);
        mAnimationTime = (int) context.getResources().getDimension(R.dimen.recommend_row_item_animation_time);

        LayoutInflater.from(context).inflate(R.layout.recommend_row_itemview, this);
        mRootView = findViewById(R.id.root_view);
        ivImage = findViewById(R.id.ivImage);
        tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setTextColor(ContextCompat.getColor(mContext, R.color.recommend_row_normal_color));
        tvTitle.setTextSize(16);
        tvTitle.setAlpha(mTextUnfocusAlpha);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attributeSet, R.styleable.PT, 0, 0);
        try {
            String text = a.getString(R.styleable.PT_text);
            tvTitle.setText(text);

            mImageId = a.getResourceId(R.styleable.PT_drawable, 0);
            mImageIdFocus = a.getResourceId(R.styleable.PT_drawable_focused, 0);
            ivImage.setImageResource(mImageId);
        } finally {
            a.recycle();
        }
    }


    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        setZ(gainFocus ? 10 : 0);
        ivImage.setImageResource(gainFocus ? mImageIdFocus : mImageId);
        tvTitle.setAlpha(gainFocus ? 1.0f : mTextUnfocusAlpha);
        tvTitle.setTextColor(gainFocus ? ContextCompat.getColor(mContext, R.color.recommend_row_focus_color) : ContextCompat.getColor(mContext, R.color.recommend_row_normal_color));
    }

}
