package com.pt.ifp.neolauncher.widget;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import com.pt.ifp.neolauncher.R;
import com.pt.ifp.neolauncher.app.NeoLauncherApp;

public class Dialog {
	public static AlertDialog.Builder newDialog(Context context) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ||
				!NeoLauncherApp.getPrefs(context).useLightDialogs()) {
			return new AlertDialog.Builder(context);
		}
		return new AlertDialog.Builder(context, R.style.LightDialog);
	}
}
