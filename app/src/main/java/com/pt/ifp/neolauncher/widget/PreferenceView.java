package com.pt.ifp.neolauncher.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.TextView;

import com.pt.ifp.neolauncher.app.NeoLauncherApp;
import com.pt.ifp.neolauncher.graphics.Ripple;
import com.pt.ifp.neolauncher.preference.Preferences;

public class PreferenceView extends TextView {
	private final Ripple ripple = Ripple.newPressRipple();

	private Preferences prefs;

	public PreferenceView(Context context) {
		super(context);
		init(context);
	}

	public PreferenceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public PreferenceView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (ripple.draw(canvas, prefs)) {
			invalidate();
		}
	}

	private void init(Context context) {
		prefs = NeoLauncherApp.getPrefs(context);
		setOnTouchListener(ripple.getOnTouchListener());
	}
}
