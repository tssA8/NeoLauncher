package com.benq.ifp.neolauncher.SearchBarComponentView;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.benq.ifp.neolauncher.R;

public class SearchBarComponent extends LinearLayout {

    private ImageView mSearchIcon;
    private ConstraintLayout mSearchEditTextContainer;
    private EditTextFocusShift mSearchEditText;
    private ImageView mClearIcon;

    public SearchBarComponent(Context context) {
        super(context);
        init(context);
    }

    public SearchBarComponent(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SearchBarComponent(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.search_bar_component, this);

        mSearchIcon = findViewById(R.id.iv_search);
        mSearchEditTextContainer = findViewById(R.id.cl_et_search_container);
        mSearchEditText = findViewById(R.id.et_search);
        mClearIcon = findViewById(R.id.iv_clear);

        // Set up listeners, etc.
        mSearchEditText.setOnTouchListener((v, event) -> {
            if (MotionEvent.ACTION_UP == event.getAction()) {
                String text = mSearchEditText.getText().toString();
                if (mOnSearchBarClickListener != null) {
                    mOnSearchBarClickListener.onSearchBarClicked(text);
                }
            }
            return false;
        });

        mSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing.
                mClearIcon.setVisibility(View.GONE);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    mOnSearchBarClickListener.onSearchBarChangeListener(s, start, before, count);
                } else {
                    mOnSearchBarClickListener.onSearchBarEmpty();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

    }

    public EditText getSearchEditText() {
        return mSearchEditText;
    }

    public ImageView getSearchIcon() {
        return mSearchIcon;
    }

    public ImageView getClearIcon() {
        return mClearIcon;
    }

    private OnSearchBarClickListener mOnSearchBarClickListener;

    public void setOnSearchBarClickListener(OnSearchBarClickListener listener) {
        mOnSearchBarClickListener = listener;
    }

    public interface OnSearchBarClickListener {
        void onSearchBarClicked(String text);

        void onSearchBarEmpty();

        void onSearchBarChangeListener(CharSequence s, int start, int before, int count);
    }


}
