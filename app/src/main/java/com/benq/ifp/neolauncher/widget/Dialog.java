package com.benq.ifp.neolauncher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import com.benq.ifp.neolauncher.R;
import com.benq.ifp.neolauncher.app.PieLauncherApp;

public class Dialog {
	public static AlertDialog.Builder newDialog(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ||
				!PieLauncherApp.getPrefs(context).useLightDialogs()) {
			return new AlertDialog.Builder(context);
		}
		return new AlertDialog.Builder(context, R.style.LightDialog);
	}
}
